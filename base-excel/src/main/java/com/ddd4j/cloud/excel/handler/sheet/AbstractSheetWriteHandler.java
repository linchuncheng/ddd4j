package com.ddd4j.cloud.excel.handler.sheet;

import com.ddd4j.cloud.excel.annotation.ExportExcel;
import com.ddd4j.cloud.excel.aop.DynamicNameAspect;
import com.ddd4j.cloud.excel.converters.LocalDateStringConverter;
import com.ddd4j.cloud.excel.converters.LocalDateTimeStringConverter;
import com.ddd4j.cloud.excel.enhance.WriterBuilderEnhancer;
import com.ddd4j.cloud.excel.exception.ExcelException;
import com.ddd4j.cloud.excel.head.HeadGenerator;
import com.ddd4j.cloud.excel.head.HeadMeta;
import com.ddd4j.cloud.excel.head.I18nHeaderCellWriteHandler;
import com.ddd4j.cloud.excel.properties.ExcelConfigProperties;
import com.ddd4j.cloud.excel.properties.SheetBuildProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.converters.Converter;
import cn.idev.excel.write.builder.ExcelWriterBuilder;
import cn.idev.excel.write.builder.ExcelWriterSheetBuilder;
import cn.idev.excel.write.handler.WriteHandler;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * 抽象缓存层SheetWriteHandler
 *
 * @author zhouhengzhe
 * @version 1.0
 * @date 2023/6/25 17:04
 */
@RequiredArgsConstructor
public abstract class AbstractSheetWriteHandler implements SheetWriteHandler, ApplicationContextAware {

    private final ExcelConfigProperties configProperties;

    private final ObjectProvider<List<Converter<?>>> converterProvider;

    private final WriterBuilderEnhancer excelWriterBuilderEnhance;

    private ApplicationContext applicationContext;

    @Getter
    @Setter
    @Autowired(required = false)
    private I18nHeaderCellWriteHandler i18nHeaderCellWriteHandler;

    @Override
    public void check(ExportExcel exportExcel) {
        if (exportExcel.fill() && !StringUtils.hasText(exportExcel.template())) {
            throw new ExcelException("@ExportExcel fill 必须配合 template 使用");
        }
    }

    @Override
    @SneakyThrows(UnsupportedEncodingException.class)
    public void export(Object o, HttpServletResponse response, ExportExcel exportExcel) {
        check(exportExcel);
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        String name = (String) Objects.requireNonNull(requestAttributes).getAttribute(DynamicNameAspect.EXCEL_NAME_KEY,
                RequestAttributes.SCOPE_REQUEST);
        if (name == null) {
            name = UUID.randomUUID().toString();
        }
        String fileName = String.format("%s%s", URLEncoder.encode(name, "UTF-8"), exportExcel.suffix().getValue());
        // 根据实际的文件类型找到对应的 contentType
        String contentType = MediaTypeFactory.getMediaType(fileName).map(MediaType::toString)
                .orElse("application/vnd.ms-excel");
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileName);
        write(o, response, exportExcel);
    }

    /**
     * 通用的获取ExcelWriter方法
     *
     * @param response    HttpServletResponse
     * @param exportExcel ExportExcel注解
     * @return ExcelWriter
     */
    @SneakyThrows(IOException.class)
    public ExcelWriter getExcelWriter(HttpServletResponse response, ExportExcel exportExcel) {
        ExcelWriterBuilder writerBuilder = EasyExcel.write(response.getOutputStream())
                .registerConverter(LocalDateStringConverter.INSTANCE)
                .registerConverter(LocalDateTimeStringConverter.INSTANCE).autoCloseStream(true)
                .excelType(exportExcel.suffix()).inMemory(exportExcel.inMemory());

        if (StringUtils.hasText(exportExcel.password())) {
            writerBuilder.password(exportExcel.password());
        }

        if (exportExcel.include().length != 0) {
            writerBuilder.includeColumnFieldNames(Arrays.asList(exportExcel.include()));
        }

        if (exportExcel.exclude().length != 0) {
            writerBuilder.excludeColumnFieldNames(Arrays.asList(exportExcel.exclude()));
        }

        if (exportExcel.writeHandler().length != 0) {
            for (Class<? extends WriteHandler> clazz : exportExcel.writeHandler()) {
                writerBuilder.registerWriteHandler(BeanUtils.instantiateClass(clazz));
            }
        }

        // 开启国际化头信息处理
        if (exportExcel.i18nHeader() && i18nHeaderCellWriteHandler != null) {
            writerBuilder.registerWriteHandler(i18nHeaderCellWriteHandler);
        }

        // 自定义注入的转换器
        registerCustomConverter(writerBuilder);

        if (exportExcel.converter().length != 0) {
            for (Class<? extends Converter> clazz : exportExcel.converter()) {
                writerBuilder.registerConverter(BeanUtils.instantiateClass(clazz));
            }
        }
        String templatePath = configProperties.getTemplatePath();
        if (StringUtils.hasText(exportExcel.template())) {
            ClassPathResource classPathResource = new ClassPathResource(
                    templatePath + File.separator + exportExcel.template());
            InputStream inputStream = classPathResource.getInputStream();
            writerBuilder.withTemplate(inputStream);
        }

        writerBuilder = excelWriterBuilderEnhance.enhanceExcel(writerBuilder, response, exportExcel, templatePath);

        return writerBuilder.build();
    }

    /**
     * 自定义注入转换器 如果有需要，子类自己重写
     *
     * @param builder ExcelWriterBuilder
     */
    public void registerCustomConverter(ExcelWriterBuilder builder) {
        converterProvider.ifAvailable(converters -> converters.forEach(builder::registerConverter));
    }

    /**
     * 构建一个 空的 WriteSheet 对象
     *
     * @param sheetBuildProperties sheet build 属性
     * @param template             模板信息
     * @return WriteSheet
     */
    public WriteSheet emptySheet(SheetBuildProperties sheetBuildProperties, String template) {
        // Sheet 编号和名称
        Integer sheetNo = sheetBuildProperties.getSheetNo() >= 0 ? sheetBuildProperties.getSheetNo() : null;
        String sheetName = sheetBuildProperties.getSheetName();

        // 是否模板写入
        ExcelWriterSheetBuilder writerSheetBuilder = StringUtils.hasText(template) ? EasyExcel.writerSheet(sheetNo)
                : EasyExcel.writerSheet(sheetNo, sheetName);

        return writerSheetBuilder.build();
    }

    /**
     * 获取 WriteSheet 对象
     *
     * @param sheetBuildProperties  sheet annotation info
     * @param dataClass             数据类型
     * @param template              模板
     * @param bookHeadEnhancerClass 自定义头处理器
     * @return WriteSheet
     */
    public WriteSheet emptySheet(SheetBuildProperties sheetBuildProperties, Class<?> dataClass, String template,
                                 Class<? extends HeadGenerator> bookHeadEnhancerClass) {

        // Sheet 编号和名称
        Integer sheetNo = sheetBuildProperties.getSheetNo() >= 0 ? sheetBuildProperties.getSheetNo() : null;
        String sheetName = sheetBuildProperties.getSheetName();

        // 是否模板写入
        ExcelWriterSheetBuilder writerSheetBuilder = StringUtils.hasText(template) ? EasyExcel.writerSheet(sheetNo)
                : EasyExcel.writerSheet(sheetNo, sheetName);

        // 头信息增强 1. 优先使用 sheet 指定的头信息增强 2. 其次使用 @ExportExcel 中定义的全局头信息增强
        Class<? extends HeadGenerator> headGenerateClass = null;
        if (isNotInterface(sheetBuildProperties.getHeadGenerateClass())) {
            headGenerateClass = sheetBuildProperties.getHeadGenerateClass();
        } else if (isNotInterface(bookHeadEnhancerClass)) {
            headGenerateClass = bookHeadEnhancerClass;
        }
        // 定义头信息增强则使用其生成头信息，否则使用 dataClass 来自动获取
        if (headGenerateClass != null) {
            fillCustomHeadInfo(dataClass, bookHeadEnhancerClass, writerSheetBuilder);
        } else if (dataClass != null) {
            writerSheetBuilder.head(dataClass);
            if (sheetBuildProperties.getExcludes().length > 0) {
                writerSheetBuilder.excludeColumnFiledNames(Arrays.asList(sheetBuildProperties.getExcludes()));
            }
            if (sheetBuildProperties.getIncludes().length > 0) {
                writerSheetBuilder.includeColumnFiledNames(Arrays.asList(sheetBuildProperties.getIncludes()));
            }
        }

        // sheetBuilder 增强
        writerSheetBuilder = excelWriterBuilderEnhance.enhanceSheet(writerSheetBuilder, sheetNo, sheetName, dataClass,
                template, headGenerateClass);

        return writerSheetBuilder.build();
    }

    private void fillCustomHeadInfo(Class<?> dataClass, Class<? extends HeadGenerator> headEnhancerClass,
                                    ExcelWriterSheetBuilder writerSheetBuilder) {
        HeadGenerator headGenerator = this.applicationContext.getBean(headEnhancerClass);
        Assert.notNull(headGenerator, "The header generated bean does not exist.");
        HeadMeta head = headGenerator.head(dataClass);
        writerSheetBuilder.head(head.getHead());
        writerSheetBuilder.excludeColumnFieldNames(head.getIgnoreHeadFields());
    }

    /**
     * 是否为Null Head Generator
     *
     * @param headGeneratorClass 头生成器类型
     * @return true 已指定 false 未指定(默认值)
     */
    private boolean isNotInterface(Class<? extends HeadGenerator> headGeneratorClass) {
        return !Modifier.isInterface(headGeneratorClass.getModifiers());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
