package com.hty.aicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.hty.aicodemother.ai.model.HtmlCodeResult;
import com.hty.aicodemother.exception.BusinessException;
import com.hty.aicodemother.exception.ErrorCode;
import com.hty.aicodemother.model.enums.CodeGenTypeEnum;

/**
 * HTML代码保存器
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{

    /**
     * 获取代码生成类型
     *
     * @return
     */
    @Override
    public CodeGenTypeEnum getCodeGenType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 保存文件
     *
     * @param result
     * @param baseDirPath
     */
    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath,"index.html", result.getHtmlCode());
    }

    /**
     * 验证输入参数
     *
     * @param result
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        //Html 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"Html代码不能为空");
        }
    }
}
