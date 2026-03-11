package com.hty.aicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hty.aicodemother.exception.BusinessException;
import com.hty.aicodemother.exception.ErrorCode;
import com.hty.aicodemother.model.enums.CodeGenTypeEnum;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象的代码文件保存器 - 模板方法模式
 *
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模板方法：保存代码的标准流程
     *
     * @param result 待保存的代码结果对象
     * @return 保存后的文件目录对象
     */
    public File saveCode(T result){
        //1、验证输入
        validateInput(result);
        //2、构建唯一目录
        String baseDirPath = buildUniqueDir();
        //3、保存文件（具体实现交给子类）
        saveFiles(result,baseDirPath);
        //4、返回文件目录对象
        return new File(baseDirPath);
    }

    /**
     * 保存单个文件
     */
    public final void writeToFile(String dirPath, String filename, String content) {
        if (StrUtil.isNotBlank(content)) {
            String filePath = dirPath + File.separator + filename;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }


    /**
     * 验证输入参数(可由子类覆盖)
     *
     * @param result 代码结果对象
     */
    protected void validateInput(T result) {
        if (result == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"代码结果对象不能为空");
        }
    }

    /**
     * 构建唯一目录路径：tmp/code_output/bizType_雪花ID
     */
    protected String buildUniqueDir() {
        String codeType = getCodeGenType().getValue();
        String uniqueDirName = StrUtil.format("{}_{}", codeType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 获取代码生成类型
     *
     *
     * @return 代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeGenType();

    /**
     * 保存文件 （具体实现交给子类）
     *
     * @param result 代码结果对象
     * @param baseDirPath 基础目录路径
     * @return
     */
    protected abstract void saveFiles(T result, String baseDirPath);
}
