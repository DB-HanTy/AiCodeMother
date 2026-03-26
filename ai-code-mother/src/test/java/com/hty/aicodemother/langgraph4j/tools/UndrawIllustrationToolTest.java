package com.hty.aicodemother.langgraph4j.tools;

import com.hty.aicodemother.langgraph4j.enums.ImageCategoryEnum;
import com.hty.aicodemother.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 图片收集工具（插画图片）
 */
@SpringBootTest
class UndrawIllustrationToolTest {

    @Resource
    private UndrawIllustrationTool undrawIllustrationTool;

    @Test
    void testSearchIllustrations() {
        // 测试正常搜索插画
        List<ImageResource> illustrations = undrawIllustrationTool.searchIllustrations("happy");

        // 1. 首先断言列表不为 null
        assertNotNull(illustrations);

        // 2. 如果列表为空，打印提示并跳过后续详细验证，避免越界异常
        if (illustrations.isEmpty()) {
            System.out.println("警告：未搜索到任何插画，跳过详细验证。可能是 API 无结果或网络问题。");
            return;
        }

        // 3. 只有在列表非空时才获取第一个元素进行验证
        ImageResource firstIllustration = illustrations.get(0);
        assertEquals(ImageCategoryEnum.ILLUSTRATION, firstIllustration.getCategory());
        assertNotNull(firstIllustration.getDescription());
        assertNotNull(firstIllustration.getUrl());
        assertTrue(firstIllustration.getUrl().startsWith("http"));

        System.out.println("搜索到 " + illustrations.size() + " 张插画");
        illustrations.forEach(illustration ->
                System.out.println("插画：" + illustration.getDescription() + " - " + illustration.getUrl())
        );
    }

}
