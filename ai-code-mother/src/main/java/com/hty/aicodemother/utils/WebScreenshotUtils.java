package com.hty.aicodemother.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hty.aicodemother.exception.BusinessException;
import com.hty.aicodemother.exception.ErrorCode;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

/**
 * 截图工具类 (ThreadLocal 优化版)
 */
@Slf4j
public class WebScreenshotUtils {

    // 使用 ThreadLocal 存储 WebDriver 实例，确保线程隔离
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    /**
     * 获取当前线程的 WebDriver 实例，不存在则创建
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            driver = initChromeDriver(1600, 900);
            driverThreadLocal.set(driver);
        }
        return driver;
    }

    /**
     * 移除并关闭当前线程的 WebDriver 实例
     * 必须在任务完成后调用，防止内存泄漏
     */
    public static void removeDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("当前线程 WebDriver 已关闭");
            } catch (Exception e) {
                log.error("关闭 WebDriver 失败", e);
            } finally {
                driverThreadLocal.remove();
            }
        }
    }

    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页 URL 不能为空");
            return null;
        }

        WebDriver driver = null;
        try {
            // 获取当前线程专属驱动
            driver = getDriver();

            // 创建临时目录
            String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots"
                    + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);

            final String IMAGE_SUFFIX = ".png";
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + IMAGE_SUFFIX;

            // 访问网页 (增加重试机制可选，此处保持原逻辑)
            driver.get(webUrl);

            // 等待页面加载完成
            waitForPageLoad(driver);

            // 截图
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功：{}", imageSavePath);

            // 压缩图片
            final String COMPRESSION_SUFFIX = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + COMPRESSION_SUFFIX;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功：{}", compressedImagePath);

            // 删除原始图片
            FileUtil.del(imageSavePath);

            return compressedImagePath;

        } catch (Exception e) {
            log.error("网页截图失败：{}", webUrl, e);
            return null;
        } finally {
            removeDriver();
        }
    }

    /**
     * 初始化 Chrome 浏览器驱动
     */
    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            System.setProperty("wdm.chromeDriverMirrorUrl", "https://registry.npmmirror.com/binary.html?path=chromedriver");
            WebDriverManager.chromedriver().useMirror().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new"); // 使用新版无头模式
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            options.addArguments("--disable-extensions");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            WebDriver driver = new ChromeDriver(options);

            // 优化：增加页面加载超时时间，避免 TimeoutException
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }

    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败：{}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    private static void compressImage(String originalImagePath, String compressedImagePath) {
        final float COMPRESSION_QUALITY = 0.3f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressedImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("压缩图片失败：{} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    private static void waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(webDriver ->
                    ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
                            .equals("complete")
            );
            Thread.sleep(2000); // 额外等待动态内容
            log.info("页面加载完成");
        } catch (Exception e) {
            log.warn("等待页面加载超时或异常，尝试继续截图", e);
        }
    }

    /**
     * 清理临时截图文件
     * 建议通过定时任务调用，或在应用启动/关闭时调用
     */
    public static void cleanupTempFiles() {
        String rootPath = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "screenshots";
        File rootDir = new File(rootPath);

        if (!rootDir.exists()) {
            log.debug("临时截图目录不存在，无需清理：{}", rootPath);
            return;
        }

        log.info("开始清理临时截图目录：{}", rootPath);
        int count = 0;

        // 遍历目录下的所有一级子目录（每个截图任务一个独立子目录）
        File[] subDirs = rootDir.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                try {
                    // 递归删除整个子目录及其内容
                    FileUtil.del(subDir);
                    count++;
                    log.debug("已清理临时目录：{}", subDir.getAbsolutePath());
                } catch (Exception e) {
                    log.error("清理临时目录失败：{}", subDir.getAbsolutePath(), e);
                }
            }
        }

        // 也可以清理根目录下直接遗留的单个文件（防御性编程）
        File[] files = rootDir.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                try {
                    FileUtil.del(file);
                    count++;
                } catch (Exception e) {
                    log.error("清理临时文件失败：{}", file.getAbsolutePath(), e);
                }
            }
        }

        log.info("临时截图文件清理完成，共清理 {} 个单元", count);
    }

}
