package com.yes4all.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yes4all.common.errors.BusinessException;
import org.apache.commons.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CommonDataUtil {
    private static final Logger log = LoggerFactory.getLogger(CommonDataUtil.class);
    public static ModelMapper modelMapper;

    public static boolean isEmpty(String str) {
        return Objects.isNull(str) || str.trim().length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !(collection == null || collection.isEmpty());
    }

    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    public static boolean getBooleanValue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public static <T> List<String> getDeclareFields(Class<? extends T> objectClass) {
        Field[] fields = objectClass.getDeclaredFields();
        List<String> lines = new ArrayList<>(fields.length);

        Arrays.stream(fields).forEach(field -> {
            field.setAccessible(true);
            lines.add(camelToSnake(field.getName()));
        });
        return lines;
    }

    public static ModelMapper getModelMapper() {
        if (Objects.isNull(modelMapper)) {
            modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        }
        return modelMapper;
    }
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isDateValidSplitPO(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat("dd-MMM-yy");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    public static String yesNoToBoolean(String value)
    {

            if(value.toUpperCase().equals("YES")){
                return "true";
            }else{
                return "false";
            }

    }
    private static String camelToSnake(String str)
    {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        str = str.replaceAll(regex, replacement).toLowerCase();
        return str;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(comparingByValueReverse());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <K, V extends Comparable<? super V>> Comparator<Map.Entry<K, V>> comparingByValueReverse() {
        return (Comparator<Map.Entry<K, V>> & Serializable)
            (c1, c2) -> c2.getValue().compareTo(c1.getValue());
    }

    public static boolean isObjectHasPropertyValue(Object obj) {
        for (Field field : obj.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object refObj = field.get(obj);
                if (refObj instanceof Collection) {
                    if (CommonDataUtil.isNotEmpty((Collection) refObj)) {
                        return true;
                    }
                } else {
                    if (CommonDataUtil.isNotNull(refObj) && field.getModifiers() != (Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("Exception occurred in processing");
                throw new BusinessException(e.getMessage());
            }
        }
        return false;
    }

    public static Map<String,Object> getAttributes(String jwtToken){
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedBody = split_string[1];

        Base64 base64Url = new Base64(true);

        String body = new String(base64Url.decode(base64EncodedBody));

        Map<String, Object> map = new HashMap<String, Object>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // convert JSON string to Map
            map = mapper.readValue(body,
                new TypeReference<Map<String, Object>>() {
                });
        } catch (Exception e) {
            log.error("Can't convert jsonString to mapObject");
        }
        return map;

    }
    public static String contentMail(String link,String purchaserOrderNo,String supplier,String object,String status,String type) {
        String htmlBody="";
        String titleLink="";
        if(type.equals("NEW")) {
            htmlBody = contentBodyMail_CreateNewPO(supplier, purchaserOrderNo);
            titleLink="View Purchase Order";
        }else{
            htmlBody = contentBodyMail_PO(supplier, purchaserOrderNo,status,object);
            if(type.equals("Confirmed") || type.equals("Cancelled") || type.equals("ConfirmedAdjustment")){
                titleLink="View Purchase Order";
            }else if(type.equals("Adjusted") || type.equals("Adjustment") ){
                titleLink="View PO Adjustment";
            }else if(type.equals("CreatedPI")){
                titleLink="View Proforma Invoice";
            }else if(type.equals("CreatedCI")){
                titleLink="View Commercial Invoice";
            }
        }
        String html = "\n <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "  <head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            "    <title>Demystifying Email Design</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
            "  </head>\n" +
            "  <style>\n" +
            "    body,\n" +
            "    table,\n" +
            "    td,\n" +
            "    a {\n" +
            "      -webkit-text-size-adjust: 100%;\n" +
            "      -ms-text-size-adjust: 100%;\n" +
            "    }\n" +
            "\n" +
            "    table,\n" +
            "    td {\n" +
            "      mso-table-lspace: 0pt;\n" +
            "      mso-table-rspace: 0pt;\n" +
            "    }\n" +
            "\n" +
            "    img {\n" +
            "      -ms-interpolation-mode: bicubic;\n" +
            "    }\n" +
            "\n" +
            "    img {\n" +
            "      border: 0;\n" +
            "      height: auto;\n" +
            "      line-height: 100%;\n" +
            "      outline: none;\n" +
            "      text-decoration: none;\n" +
            "    }\n" +
            "\n" +
            "    table {\n" +
            "      border-collapse: collapse !important;\n" +
            "    }\n" +
            "\n" +
            "    body {\n" +
            "      height: 100% !important;\n" +
            "      margin: 0 !important;\n" +
            "      padding: 0 !important;\n" +
            "      width: 100% !important;\n" +
            "    }\n" +
            "\n" +
            "    a[x-apple-data-detectors] {\n" +
            "      color: inherit !important;\n" +
            "      text-decoration: none !important;\n" +
            "      font-size: inherit !important;\n" +
            "      font-family: inherit !important;\n" +
            "      font-weight: inherit !important;\n" +
            "      line-height: inherit !important;\n" +
            "    }\n" +
            "\n" +
            "    div[style*=\"margin: 16px 0;\"] {\n" +
            "      margin: 0 !important;\n" +
            "    }\n" +
            "  </style>\n" +
            "  <body\n" +
            "    style=\"background-color: #fff; margin: 0 !important; padding: 0 !important\"\n" +
            "  >\n" +
            "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">\n" +
            "      <tr\n" +
            "        style=\"\n" +
            "          box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.25);\n" +
            "          position: relative;\n" +
            "          z-index: 1;\n" +
            "          border-top: 8px solid #194b9b;\n" +
            "        \"\n" +
            "      >\n" +
            "        <td bgcolor=\"#FFF\" align=\"center\" style=\"padding: 0px 10px 0px 10px\">\n" +
            "          <div style=\"height: 90px; padding: 20px 0; box-sizing: border-box\">\n" +
            "            <img\n" +
            "              src=\"https://cdn.shopify.com/s/files/1/0595/0082/2724/files/logo_yes4all_color-2.png\"\n" +
            "              style=\"height: 60px\"\n" +
            "            />\n" +
            "          </div>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "      <tr>\n" +
            "        <td bgcolor=\"#fff\" align=\"center\" style=\"padding: 0px 10px 0px 10px\">\n" +
            "          <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"720\">\n" +
            "            <tr>\n" +
            "              <td bgcolor=\"#ffffff\" align=\"left\">\n" +
            "                <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "                  <tr>\n" +
            "                    <th\n" +
            "                      style=\"\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 600;\n" +
            "                        line-height: 50px;\n" +
            "                        color: #fff;\n" +
            "                        padding: 40px;\n" +
            "                        width: 55%;\n" +
            "                      \"\n" +
            "                    ></th>\n" +
            "                  </tr>\n" +
            htmlBody+
            "\n" +
            "                  <tr>\n" +
            "                    <th\n" +
            "                      align=\"center\"\n" +
            "                      valign=\"top\"\n" +
            "                      style=\"\n" +
            "                        padding-left: 30px;\n" +
            "                        padding-right: 15px;\n" +
            "                        padding-bottom: 10px;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                        padding-top: 10px;\n" +
            "                      \"\n" +
            "                    >\n" +
            "                      <a\n" +
            "                        href=\"" + link + "\"\n" +
            "                        target=\"_blank\"\n" +
            "                        style=\"\n" +
            "                          text-decoration: unset;\n" +
            "                          background: #194b9b;\n" +
            "                          border-radius: 5px;\n" +
            "                          width: 275px;\n" +
            "                          display: inline-block;\n" +
            "                          text-align: center;\n" +
            "                          color: #fff;\n" +
            "                          padding: 12px 0;\n" +
            "                          margin: 40px 0;\n" +
            "                        \"\n" +
            "                        >"+titleLink+"</a\n" +
            "                      >\n" +
            "                    </th>\n" +
            "                  </tr>\n" +
            "\n" +
            "                  <tr>\n" +
            "                    <td\n" +
            "                      align=\"center\"\n" +
            "                      style=\"\n" +
            "                        padding-bottom: 15px;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 14px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                      \"\n" +
            "                    >\n" +
            "                      Thank You | Yes4All\n" +
            "                    </td>\n" +
            "                  </tr>\n" +
            "                  <tr>\n" +
            "                    <td\n" +
            "                      align=\"center\"\n" +
            "                      style=\"\n" +
            "                        padding: 15px 0;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 16px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                        border-top: 1px solid #ccc;\n" +
            "                      \"\n" +
            "                    ></td>\n" +
            "                  </tr>\n" +
            "                  <tr>\n" +
            "                    <td\n" +
            "                      align=\"center\"\n" +
            "                      style=\"\n" +
            "                        padding-bottom: 15px;\n" +
            "                        font-family: Helvetica, Arial, sans-serif;\n" +
            "                        font-size: 14px;\n" +
            "                        font-weight: 400;\n" +
            "                        line-height: 25px;\n" +
            "                      \"\n" +
            "                    >\n" +
            "                      (<span style=\"color: red\">*</span>) Please do not respond\n" +
            "                      to this email as responses are not monitored. <br />\n" +
            "                      If you have any questions, please\n" +
            "                      <a\n" +
            "                        href=\"mailto:nguyen.nguyenvt@yes4all.com\"\n" +
            "                        style=\"text-decoration: none\"\n" +
            "                        >email support</a\n" +
            "                      >.\n" +
            "                    </td>\n" +
            "                  </tr>\n" +
            "                </table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "            <tr>\n" +
            "              <td bgcolor=\"#ffffff\" align=\"center\">\n" +
            "                <table\n" +
            "                  width=\"100%\"\n" +
            "                  border=\"0\"\n" +
            "                  cellspacing=\"0\"\n" +
            "                  cellpadding=\"0\"\n" +
            "                ></table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "          </table>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "    </table>\n" +
            "  </body>\n" +
            "</html>\n" +
            "\n";
        return html;
    }
     public static String contentBodyMail_CreateNewPO(String supplier,String purchaserOrderNo){
         String htmlBody="                  <tr>" +
             "                             <td" +
             "                                 align=\\\"left\\\"\\\n" +
             "                                valign=\\\"top\\\"\\\n" +
             "                                  style=\\\"\\n" +
             "                                   padding-left: 60px;\n" +
             "                                  padding-right: 60px;\n" +
             "                                  padding-bottom: 10px;\n" +
             "                                   padding-top: 16px;\n" +
             "                                   font-family: Helvetica, Arial, sans-serif;\n" +
             "                                   font-size: 18px;\n" +
             "                                  font-weight: 400;\n" +
             "                                    line-height: 30px;\n" +
             "                                  \n" +
             "                               > Dear  <strong>  " + supplier + " </strong>,<br /> \n" +
             "                                We send you a new purchase order <strong> " + purchaserOrderNo + " </strong> \n" +
             "                                 <br />\n" +
             "                                Please check and confirm by clicking the button bellow. \n" +
             "                             </td> \n" +
             "                            </tr> ";
         return htmlBody;
     }
    public static String contentBodyMail_PO(String supplier,String purchaserOrderNo,String status,String object){
        String htmlBody="                  <tr>" +
            "                             <td" +
            "                                 align=\\\"left\\\"\\\n" +
            "                                valign=\\\"top\\\"\\\n" +
            "                                  style=\\\"\\n" +
            "                                   padding-left: 60px;\n" +
            "                                  padding-right: 60px;\n" +
            "                                  padding-bottom: 10px;\n" +
            "                                   padding-top: 16px;\n" +
            "                                   font-family: Helvetica, Arial, sans-serif;\n" +
            "                                   font-size: 18px;\n" +
            "                                  font-weight: 400;\n" +
            "                                    line-height: 30px;\n" +
            "                                  \n" +
            "                               > \n" +
            "                                "+object+" <strong> " + purchaserOrderNo + " </strong> has been "+status+" by "+(!supplier.equals("Yes4all")?"supplier <strong>" + supplier + ".</strong>":"Ye4all")+" \n" +
            "                                 <br />\n" +
            "                                Please check and confirm by clicking the button bellow. \n" +
            "                             </td> \n" +
            "                            </tr> ";
        return htmlBody;
    }

}
