package user.creepwang.listener;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang.StringUtils;
import user.creepwang.entity.ImportSettings;
import user.creepwang.entity.SysDict;
import user.creepwang.entity.XlsxEntity;
import user.creepwang.entity.XmtzExcelImport;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author CreepWang
 * @date 2022/7/8 15:43
 * @Version 2.0 通用版不加权限
 * 通用Excel解析工具
 */

public   class ExcelAnalysisUtilV5 {



    /**
     * CreepWang
     * 通用Excel解析方法
     *
     * @param
     * @param
     * @return resultCode(903 : 文件异常 906 数据已经被锁定无法变动 907 : 用户底下缺少管理部门 300 : 导入数据里面有数据填写不规范 901 : 错误 200 成功)
     */
    public static Map<String, Object> AnalysisExcelReturnDataMap(String FileName, List<ImportSettings> totalImportSettings, Integer pageNo,String TypeCode,Integer headNum, PraseExt praseExt) {

        Map<String, Object> mapReturn = new HashMap<>();



        // 上传并返回新文件路径名称
        try {


                List<ImportSettings> importSettingsList  =totalImportSettings.stream().filter(s -> StringUtils.equals(TypeCode,s.getAttrId())&&StringUtils.equals(pageNo+"",s.getPageNo())
                ).collect(Collectors.toList());


                if (importSettingsList.size() == 0) {
                    mapReturn.put("resultCode", "905");
                    mapReturn.put("resultMessage", "importSettingsList" + pageNo + "参数异常");
                    return mapReturn;
                }



                ExcelListenerV5 excelListener = new ExcelListenerV5(pageNo,importSettingsList, headNum, praseExt);
                EasyExcel.read(FileName, XlsxEntity.class, excelListener)
                        .sheet(pageNo - 1)
                        .autoTrim(Boolean.TRUE).
                        doRead();
                mapReturn = excelListener.getMapReturn();
                List<Map> listData = excelListener.getDataList();
                mapReturn.put("dataList", listData);

                return mapReturn;

        } catch (Exception e) {
            e.printStackTrace();
            mapReturn.put("resultCode", "903");
            mapReturn.put("resultMessage", "导入未知异常");
            return mapReturn;
        }

    }

}

