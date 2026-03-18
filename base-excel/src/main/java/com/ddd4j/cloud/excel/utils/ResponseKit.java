package com.ddd4j.cloud.excel.utils;

import com.ddd4j.cloud.excel.exception.ExcelException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.io.FileUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * HttpServletResponse工具类
 *
 * @author zhouhengzhe
 */
@Slf4j
public class ResponseKit {

    /**
     * 获取response
     *
     * @return
     */
    public static HttpServletResponse getResponse() {

        HttpServletResponse response = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getResponse();

        return response;
    }


    /**
     * 下载文件
     *
     * @param file
     */
    public static void download(File file) {

        download(file, file.getName());
    }

    /**
     * 下载文件
     *
     * @param file
     * @param fileName
     */
    public static void download(File file, String fileName) {

        download(file, fileName, "application/octet-stream");
    }

    /**
     * 下载文件
     *
     * @param file
     * @param fileName
     * @param contentType
     */
    public static void download(File file, String fileName, String contentType) {

        HttpServletResponse response = getResponse();
        response.setContentType(contentType);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("FileName", fileName);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        OutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(FileUtil.readBytes(file));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ExcelException(e.getMessage());
        }
    }
}
