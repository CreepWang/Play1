package user.creepwang; /**
 * @author CreepWang
 * @date 2023/3/20 12:06
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import user.creepwang.entity.ImportSettings;
import user.creepwang.listener.ExcelAnalysisUtilV5;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ccc {

    private static String FilePath="导入文件.xlsx";
    private static Boolean isSucessAddPost;

    public static void main(String[] args) throws Exception {
        String filePath = ccc.class.getClassLoader().getResource("pz.json").getPath();
        // 将路径中的 %20 替换为空格
        filePath = filePath.replace("%20", " ");
        File jsonFile = new File(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = objectMapper.writeValueAsString(objectMapper.readValue(jsonFile, Object.class));

        List<ImportSettings> importSettingsList = objectMapper.readValue(jsonStr, new TypeReference<List<ImportSettings>>() {
        });

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            LocalDateTime now = LocalDateTime.now();
            // 将当前时间格式化为数字字符串，例如：20230425164423
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
           String formattedDateTime = now.format(formatter);
            BrowserContext context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(Paths.get(formattedDateTime+"/")));
            Page page = context.newPage();
            page.onRequest(request -> {
                        if(request.url().contains("xmtzterminal/add")){
                            isSucessAddPost=true;
                            System.out.println(">> " + request.method() + " " + request.url());
                        }

                    }
            );
           //page.onResponse(response -> System.out.println("<<" + response.status() + " " + response.url()));


            Map<String, Object> sheet1DataMapList = ExcelAnalysisUtilV5.AnalysisExcelReturnDataMap(FilePath, importSettingsList, 1, "zddr", 2, null);
            String resultCode = sheet1DataMapList.get("resultCode")+"";
            if(StringUtils.equals("901",resultCode)){
                System.out.println("表头错了"+sheet1DataMapList.toString());
                return; //表头错误
            }
            List<Map<String, Object>> datas = (List<Map<String, Object>>) sheet1DataMapList.get("dataList");
            System.out.println(datas.toString());
            Login(page);
            doSbmit(page,datas);
            System.out.println("开始生成excel。。。。。。。。。。。。。");
            createLogExcel(datas,formattedDateTime);
            context.close();
            browser.close();
        }
    }

    public static void Login(Page page) {
        System.out.println("开始调用登录");
        page.navigate("http://192.168.10.22:6888/xcxm/#/login");

        page.locator("input[name='username']").type("superSheng");
        page.locator("input[name='password']").type("Rxkj@2022");
        page.querySelector("span:text('登录')").click();
 /*      if( page.locator("h1:text('江西省SHTD调度指挥平台')")!=null){
           System.out.println("登录成功");
       }
         page.waitForSelector("h1:text('江西省SHTD调度指挥平台')", new Page.WaitForSelectorOptions().setTimeout(5000));*/
        System.out.println("元素已出现");
        page.locator("p:text('底账管理子系统')").click();
        page.locator("span:text('替代底账')").click();
        page.locator("span:text('报审底账')").click();
        page.getByText("录入").first().click();
        System.out.println("登录结束");
        page.waitForTimeout(2000);
    }


    public static void doSbmit (Page page,List<Map<String, Object>> datas) {

        datas.forEach(data -> {
            isSucessAddPost=false;
            System.out.println("data="+data.toString());
            if( StringUtils.equals("0",data.get("hasError")+"")){
                System.out.println("开始新增");
                page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("新增")).click();
                page.getByPlaceholder("请选择终端类型").click();
                page.getByRole(AriaRole.LISTITEM).filter(new Locator.FilterOptions().setHasText(data.get("a0")+"")).click();
                page.getByPlaceholder("请选择采购年月").click();
                page.getByText("六月").click();

                page.getByPlaceholder("请输入品牌名称").fill(data.get("a2")+"");
                page.getByPlaceholder("请输入部署位置").fill(data.get("a8")+"");
                page.getByPlaceholder("请输入使用人姓名").fill(data.get("a5")+"");
                page.getByPlaceholder("请输入使用系统名称").fill(data.get("a9")+"");
                page.getByPlaceholder("请输入采购单价").fill(data.get("a3")+"");
                //业务用途
                page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(data.get("a4")+"")).first().click();
                //是否涉密
                page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(data.get("a7")+"")).first().click();
                //是否涉密
                page.getByRole(AriaRole.RADIO, new Page.GetByRoleOptions().setName(data.get("a6")+"")).first().click();

                page.locator("span:text('提交')").click();
                if(isSucessAddPost){
                    //提交成功  严格一点的话还要判断接口返回
                    data.put("failRemark","提交成功");
                }else{
                    List<ElementHandle> elementHandles = page.querySelectorAll("div .el-form-item__error");
                    StringBuffer appendErrors = new StringBuffer();
                    for ( ElementHandle elementHandle : elementHandles){
                        System.out.println(elementHandle.textContent());
                        appendErrors.append(elementHandle.textContent().trim()+" ");
                        System.out.println(elementHandle.textContent());
                            }
                    if(appendErrors!=null){
                        data.put("hasError","1");
                        data.put("failRemark",data.get("failRemark")+" "+appendErrors.toString());
                    }
                    page.locator("span:text('返回')").click();
                }
                page.waitForTimeout(1000);
            }

        });

    }


    public static void createLogExcel(List<Map<String, Object>> datas,String formattedDateTime) throws IOException {

        System.out.println("开始生成Excel");
        Workbook workbook = new XSSFWorkbook(FilePath);
        Sheet sheet = workbook.getSheetAt(0);

        Map<String, String> collect =new HashMap<>();
        datas.forEach(s->{
            System.out.println("s=="+s.toString());
            collect.put(s.get("rowNum")+"",s.get("failRemark")+"");
        });
        System.out.println("collect==="+collect.toString());

        for (int rowNum = 0; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row==null){
                continue;
            }
            String remark= collect.get((rowNum + 1)+"");
            Font font = workbook.createFont();
            if(StringUtils.equals("提交成功",remark)){
                font.setColor(Font.COLOR_NORMAL);
            }else {
                font.setColor(Font.COLOR_RED);
            }
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFont(font);
            int lastCellNum =row.getLastCellNum()+1;
            Cell celltmp = row.createCell(lastCellNum);
            celltmp.setCellValue(remark);
            celltmp.setCellStyle(cellStyle);
        }
        String newFilename = formattedDateTime+"/记录文件.xlsx";
        FileOutputStream outputStream = new FileOutputStream(new File(newFilename));
        workbook.write(outputStream);
       // workbook.close();
        outputStream.close();
        System.out.println("文件生成成功");

    }
}
