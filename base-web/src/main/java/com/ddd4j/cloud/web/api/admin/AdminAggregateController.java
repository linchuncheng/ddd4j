package com.ddd4j.cloud.web.api.admin;

import com.ddd4j.cloud.web.api.AggregateController;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理端聚合API
 *
 * @author Jensen
 * @公众号 架构师修行录
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "模型聚合")
public class AdminAggregateController implements AggregateController {

}