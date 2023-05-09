package user.creepwang.utools;



import org.apache.commons.lang.StringUtils;
import user.creepwang.entity.SysDict;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelCheckUtils {
//    private static String regExp = "^((13[0-9])|(14[5,7,9])|(15[0-3,5-9])|(166)|(17[3,5,6,7,8])" +
//            "|(18[0-9])|(19[8,9]))\\d{8}$";021-

    private static String regExp = "^((1[3456789]\\d{9}))$";

    private static Pattern patternTel = Pattern.compile(regExp);

    private static final Pattern patternData = Pattern.compile("[^(0-9)]");

    private static Pattern patternInt = Pattern.compile("[0-9]*");


    public static String checkMaxLength(String colName, String value, int maxLength) {

        if (!StringUtils.isNotEmpty(value)) {
            return colName + "的值不能为空";
        }
        return value.length() > maxLength ? (colName + "长度大于规定最大长度 " + maxLength) : "";
    }


    public static String checkMaxNum(String colName, String value, Integer maxNum) {

        BigDecimal bigDecimal = null;
        BigDecimal maxNumBigDecimal = new BigDecimal(maxNum);
        try {
            bigDecimal = new BigDecimal(value);
        } catch (Exception e) {
            return colName + ":" + value + " 不是规定的数字类型  ";
        }

        return bigDecimal.compareTo(maxNumBigDecimal) == 1 ? colName + "大于规定值 " + maxNum : "";
    }


    public static Boolean checkInWhichTheBool(String value, String InWhichStr) {

        return Arrays.asList(InWhichStr.split(","))
                .stream().anyMatch(s -> StringUtils.equals(s, value));

    }

    public static String checkMinNum(String colName, String value, Integer minNum) {

        BigDecimal bigDecimal = null;
        BigDecimal maxNumBigDecimal = new BigDecimal(minNum);
        try {
            bigDecimal = new BigDecimal(value);
        } catch (Exception e) {
            return colName + ":" + value + " 不是规定的数字类型  ";
        }
        return bigDecimal.compareTo(maxNumBigDecimal) == -1 ? colName + "小于规定值 " + minNum : "";
    }

    public static String checkInWhichThe(String colName, String value, String InWhichStr) {

    /*    if (!StringUtils.isNotEmpty(value)) {
            return colName + "的值不能为空";
        }*/

        return Arrays.asList(InWhichStr.replace("[", "").replace("]", "").split(","))
                .stream().anyMatch(s -> StringUtils.contains(s, value)) ? "" : colName + "不在规定值" + InWhichStr + "之内  ";

    }

    public static String checkIsNumeric(String colName, String value) {

        BigDecimal bigDecimal = null;
        try {
            bigDecimal = new BigDecimal(value);
        } catch (Exception e) {
            return colName + "不是规定的数字类型  ";
        }
        return "";
    }


    public static String checkIsInt(String colName, String value) {

        return patternInt.matcher(value).matches() ? "" : colName + "必须填写整数类型";
    }


    public static Boolean IsInt(String value) {

        return patternInt.matcher(value).matches();
    }

    public static String checkInList(String colName, String value, List<SysDict> sysDictList) {

  /*      if (!StringUtils.isNotEmpty(value)) {
            return colName + "的值不能为空 ";
        }*/
        return sysDictList.stream().anyMatch(s -> value.equals(s.getDictName())) ? "" : "  " + colName + "不在规定值的数据字典之内  ";
    }


    public static String isPhone(String colName, String colValue) {

        Matcher m = null;
        boolean isPhone = false;
        m = patternTel.matcher(colValue);
        isPhone = m.matches();
        if (isPhone) {
            return "";
        } else {
            return " " + colName + ":" + colValue + "不是一个合法的电话值 ";
        }

    }

    public static String isValidDate(String colName, String colValue) {

        String tmp = patternData.matcher(colValue).replaceAll("").trim();
        if (tmp.length() != 8) {
            return " " + colName + ":" + colValue + "不是一个合法的日期值 ";
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
            format.setLenient(false);
            format.parse(tmp);
            return tmp;
        } catch (ParseException e) {
            return " " + colName + ":" + colValue + "不是一个合法的日期值";
        }
    }

    public static String isValidDateYM(String colName, String colValue) {
        String tmp = patternData.matcher(colValue).replaceAll("").trim();
        if (tmp.length() > 6) {
            tmp = tmp.substring(0, 6);
        }
        ;

        if (tmp.length() != 4 && tmp.length() != 6) {
            return " " + colName + ":" + colValue + "不是一个合法的日期值 ";
        }
        DateFormat format = null;
        if (tmp.length() == 4) {
            format = new SimpleDateFormat("yyyy");
        }
        if (tmp.length() == 6) {
            format = new SimpleDateFormat("yyyyMM");
        }
        try {
            format.setLenient(false);
            Date parse = format.parse(tmp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            tmp = sdf.format(parse);
            return tmp;
        } catch (ParseException e) {
            return " " + colName + ":" + colValue + " 不是一个合法的日期值  ";
        }
    }


    public static boolean isAllFieldNull(Object object) {
        boolean flag = true;

        Class clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            //设置属性是可以访问的(私有的也可以)
            field.setAccessible(true);
            Object value = null;
            try {
                value = field.get(object);
                // 只要有1个属性不为空,那么就不是所有的属性值都为空
                if (value != null) {
                    flag = false;
                    break;
                }
            } catch (IllegalAccessException e) {
            }
        }

        return flag;
    }


    public static String isFixedPhone(String colName, String colValue) {
        //02164470087
        if (colValue.matches("^((0\\d{2,3})?[-]?\\d{7,8})$")) {
            return "";
        } else {
            return " " + colName + ":" + colValue + "不是一个合法的固定电话值 ";
        }

    }

    //是否为请求链接
    public static String isUrl(String colName, String colValue) {
        if (colValue.startsWith("http://") || colValue.startsWith("https://")) {
            return "";
        } else {
            return " " + colName + ":" + colValue + "不是一个合法的网址链接，必须以http://或https://开头";
        }
    }

}
