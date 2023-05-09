package user.creepwang.listener;

import com.alibaba.excel.context.AnalysisContext;

import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.apache.commons.lang.StringUtils;

import user.creepwang.entity.ImportSettings;
import user.creepwang.entity.XlsxEntity;
import user.creepwang.entity.XmtzExcelImport;
import user.creepwang.entity.XmtzImportFail;
import user.creepwang.utools.ExcelCheckUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ExcelListenerV5 extends AnalysisEventListener<XlsxEntity> {

    private volatile int totalCount = 0;
    private volatile int rowIndexlCount = 1;
    private volatile int errorCount = 0;
    List<XlsxEntity> list = new ArrayList<>();
    private Map<String, Object> mapReturn = new HashMap<>();
    private String flag;
    private String headErMess = "";
    private List<ImportSettings> importSettingsList;
    private List<Map> dataList = new ArrayList<Map>();
    private Boolean hasError = false;
    private Integer headNum = 1;



    private PraseExt praseExt;
    private Integer pageNo = 1;


    public ExcelListenerV5(Integer pageNo, List<ImportSettings> importSettingsList, Integer headNum, PraseExt praseExt) {
        this.flag = "true";
        this.importSettingsList = importSettingsList;
        this.praseExt = praseExt;
        this.pageNo = pageNo;
        if (headNum != null) {
            this.headNum = headNum;
        }
    }

    public Map<String, Object> getMapReturn() {
        return mapReturn;
    }

    public List<Map> getDataList() {
        return dataList;
    }


    @Override
    public boolean hasNext(AnalysisContext context) {
        return "true".equals(flag);
    }

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data
     * @param context
     */
    @Override
    public void invoke(XlsxEntity data, AnalysisContext context) {


        rowIndexlCount++;
        if (ExcelCheckUtils.isAllFieldNull(data)) {
            return;
        }
        totalCount++;
        if (totalCount < headNum - 1) {
            return;
        }
        Map<String, Object> headMap = new HashMap<>();
        headMap = JSON.parseObject(JSON.toJSONString(data), new TypeReference<Map<String, Object>>() {
        });
        headMap.put("hasError","0");
        if (headNum != 1 && totalCount == headNum - 1) {
            try {
                for (int i = 0; i < importSettingsList.size(); i++) {
                    if (!headMap.get("a" + i).equals(importSettingsList.get(i).getCellName())) {
                        headErMess = headErMess + "表头第" + (i + 1) + "列 导入数据为:" + headMap.get("a" + i) + "  模板为:" + importSettingsList.get(i).getCellName();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                flag = "false";
                mapReturn.put("resultCode", "901");
                mapReturn.put("resultMessage", "上传模板与系统模板不匹配，请使用平台模板上传数据");
                XmtzImportFail xmtzImportFail = new XmtzImportFail();
                xmtzImportFail.setCreateTime(LocalDateTime.now());
                xmtzImportFail.setRowNum(rowIndexlCount);
                xmtzImportFail.setFailRemark(headErMess);
                xmtzImportFail.setPageNo(pageNo + "");
                return;
            }
            //表头有问题直接flag设为false直接走
            if (StringUtils.isNotBlank(headErMess)) {
                flag = "false";
                mapReturn.put("resultCode", "901");
                mapReturn.put("resultMessage", "上传模板与系统模板不匹配，请使用平台模板上传数据");
                XmtzImportFail xmtzImportFail = new XmtzImportFail();
                xmtzImportFail.setCreateTime(LocalDateTime.now());
                xmtzImportFail.setRowNum(rowIndexlCount);
                xmtzImportFail.setFailRemark(headErMess);
                xmtzImportFail.setPageNo(pageNo + "");

                return;
            }   else {
            //   saveStartLog();
            return;
        }
        }

        headMap.put("rowNum", rowIndexlCount);
        //校验数据
        String rules;
        String colValue;
        String[] ruleArray;
        String letfStr;
        String rightStr;
        String headColName;
        StringBuffer checkMessBuffer = new StringBuffer();
        for (int i = 0; i < importSettingsList.size(); i++) {
            colValue = (String) headMap.get("a" + i);
            rules = importSettingsList.get(i).getCellRule();
            headColName = importSettingsList.get(i).getCellName();


            if (StringUtils.contains(rules, "notnull_@")) {
                if (StringUtils.isEmpty(colValue)) {
                    checkMessBuffer.append(headColName + "的值不能为空  ");
                    continue;
                }
            } else {
                if (StringUtils.isBlank(colValue)) {
                    headMap.put("a" + i, "");
                    continue;
                }
            }



            if (StringUtils.isNotBlank(rules)) {
                ruleArray = rules.split(";");
                for (String rule : ruleArray) {
                    letfStr = rule.substring(0, rule.indexOf("_"));
                    rightStr = StringUtils.substringAfter(rule, "_");


                    if ("maxsize".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.checkMaxLength(headColName, colValue, Integer.parseInt(rightStr)));
                    } else if ("maxnum".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.checkMaxNum(headColName, colValue, Integer.parseInt(rightStr)));
                    } else if ("minmum".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.checkMinNum(headColName, colValue, Integer.parseInt(rightStr)));
                    } else if ("inwhich".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.checkInWhichThe(headColName, colValue, rightStr));
                    } else if ("isnum".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.checkIsNumeric(headColName, colValue));
                    } else if ("isint".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.checkIsInt(headColName, colValue));
                    } else if ("isphone".equals(letfStr)) {
                        checkMessBuffer = checkMessBuffer.append(ExcelCheckUtils.isPhone(headColName, colValue));
                    } else if ("isFixedPhone".equals(letfStr)) {
                        checkMessBuffer.append(ExcelCheckUtils.isFixedPhone(headColName, colValue));
                    } else if ("isUrl".equals(letfStr)) {
                        checkMessBuffer.append(ExcelCheckUtils.isUrl(headColName, colValue));
                    }else if ("isdate".equals(letfStr)) {
                        String tmp = ExcelCheckUtils.isValidDate(headColName, colValue);
                        if (tmp.contains("合法")) {
                            checkMessBuffer = checkMessBuffer.append(tmp);
                        } else {
                            if (!hasError) {
                                headMap.put("a" + i, tmp);
                            }
                        }
                    } else if ("isdateYM".equals(letfStr)) {
                        String tmp = ExcelCheckUtils.isValidDateYM(headColName, colValue);
                        if (tmp.contains("合法")) {
                            checkMessBuffer.append(tmp);
                        } else {
                            if (!hasError) {
                                headMap.put("a" + i, tmp);
                            }
                        }
                    }

                }
            }


        }

        label:
        //抽象增强解析
        if (praseExt != null) {
            checkMessBuffer = checkMessBuffer.append(this.praseExt.praseExt(headMap));
        }



           // hasError = true;
            errorCount++;


        if (checkMessBuffer != null && checkMessBuffer.length() > 0) {
            headMap.put("failRemark",checkMessBuffer.toString());
            headMap.put("rowNum",rowIndexlCount);
            headMap.put("errorPageNum",pageNo);
            headMap.put("hasError","1");
        }



        //存储代码了


            dataList.add(headMap);





    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {


            //  saveEndLog();
            if (errorCount == 0) {
                mapReturn.put("resultCode", "200");
                mapReturn.put("resultMessage", "上传成功 成功入库条数:" + (totalCount + 1 - headNum));
            } else {
                mapReturn.put("resultCode", "300");
                mapReturn.put("resultMessage", "导入数据中有" + errorCount + "条错误数据 请下载修改后重新上传");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void invokeHeadMap(Map headMap, AnalysisContext context) {
        if (headNum == 1) {
            try {
                for (int i = 0; i < importSettingsList.size(); i++) { //headMap.get(i)
                    if (!importSettingsList.get(i).getCellName().equals(headMap.get(i))) {
                        headErMess = headErMess + "表头第" + (i + 1) + "列 导入数据为:" + headMap.get(i) + "  模板为:" + importSettingsList.get(i).getCellName() + "  ";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                flag = "false";
                mapReturn.put("resultCode", "901");
                mapReturn.put("resultMessage", "导入表列数与规定模板不一致 请检查数据后重新导入");
                return;
            }
            if (StringUtils.isNotBlank(headErMess)) {
                flag = "false";
                mapReturn.put("resultCode", "901");
                mapReturn.put("resultMessage", headErMess);

                XmtzImportFail xmtzImportFail = new XmtzImportFail();
                xmtzImportFail.setCreateTime(LocalDateTime.now());
                xmtzImportFail.setRowNum(rowIndexlCount);
                xmtzImportFail.setFailRemark(headErMess);
                xmtzImportFail.setPageNo(String.valueOf(pageNo));
                //V5 变化 直接存了
                //  cachedErrorList.add(xmtzImportFail);
                // mapReturn.put("cachedErrorList",cachedErrorList);
                return;
            } else {
                //saveStartLog();
            }

        }


    }



    ;


}
