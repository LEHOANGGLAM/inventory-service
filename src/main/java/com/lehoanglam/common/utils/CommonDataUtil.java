package com.yes4all.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.yes4all.common.errors.BusinessException;
import com.yes4all.domain.User;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.text.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.binary.Base64;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonDataUtil {

    private static final Logger log = LoggerFactory.getLogger(CommonDataUtil.class);
    public static ModelMapper modelMapper;

    public static final String SPACE = " ";
    public static final String DEFAULT_SYSTEM_USERNAME = "System";

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

    public static String toEmpty(String str) {
        return isEmpty(str) ? "" : str;
    }

    public static Integer toZero(Integer number) {
        return isNull(number) ? 0 : number;
    }

    public static <T> List<String> getDeclareFields(Class<? extends T> objectClass) {
        Field[] fields = objectClass.getDeclaredFields();
        List<String> lines = new ArrayList<>(fields.length);

        Arrays
            .stream(fields)
            .forEach(field -> {
                field.setAccessible(true);
                lines.add(camelToSnake(field.getName()));
            });
        return lines;
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("Đ", "D");
    }

    public static ModelMapper getModelMapper() {
        if (Objects.isNull(modelMapper)) {
            modelMapper = new ModelMapper();
            modelMapper.getConfiguration().setSkipNullEnabled(true).setMatchingStrategy(MatchingStrategies.STRICT);
        }
        return modelMapper;
    }

    public static List<String> convertStringToListTrimAndUpper(String originString, String regex) {
        List<String> listString = new ArrayList<>();
        if (CommonDataUtil.isNotEmpty(originString)) {
            listString =
                Splitter.on(regex).trimResults().splitToList(originString).stream().map(String::toUpperCase).collect(Collectors.toList());
        }
        return listString;
    }

    public static List<String> convertStringToListTrim(String originString, String regex) {
        List<String> listString = new ArrayList<>();
        if (CommonDataUtil.isNotEmpty(originString)) {
            listString = Splitter.on(regex).trimResults().splitToList(originString);
        }
        return listString;
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

    public static boolean isPositiveInteger(CharSequence strNum) {
        if (strNum == null || strNum.length() == 0) {
            return false;
        }
        try {
            return Integer.parseInt(strNum.toString()) >= 0;
        } catch (Exception exception) {
            return false;
        }
    }

    public static String moneyFormatter(BigDecimal amount) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00 USD");
        return formatter.format(amount);
    }

    public static String quantityFormatter(Integer quantity) {
        DecimalFormat formatter = new DecimalFormat("#,##0");
        return formatter.format(quantity);
    }

    public static boolean isDateValid(String date) {
        try {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String yesNoToBoolean(String value) {
        if (value.equalsIgnoreCase("YES")) {
            return "true";
        } else {
            return "false";
        }
    }

    private static String camelToSnake(String str) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1_$2";
        str = str.replaceAll(regex, replacement).toLowerCase();
        return str;
    }

    public static String getCodeByValue(String prefix, String value) {
        String result = fillCharLeft(value, 3, '0');
        if (isNotEmpty(result)) {
            LocalDateTime ldt = DateUtil.convertInstantToLocalDateTime(Instant.now());
            return (
                prefix +
                "." +
                String.valueOf(ldt.getYear()).substring(2) +
                "." +
                fillCharLeft(ldt.getMonthValue() + "", 2, '0') +
                "." +
                result
            );
        }
        return null;
    }

    public static String fillCharLeft(String value, int countChar, Character characterFill) {
        String result = null;
        try {
            result = Strings.padStart(value, countChar, characterFill);
        } catch (Exception e) {
            log.error("Exception occurred in processing");
        }
        return result;
    }

    public static String joiningString(String delimiter, String... values) {
        return Stream.of(values).filter(CommonDataUtil::isNotEmpty).collect(Collectors.joining(delimiter));
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
        return (Comparator<Map.Entry<K, V>> & Serializable) (c1, c2) -> c2.getValue().compareTo(c1.getValue());
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

    public static Integer getIntValueOrDefault(Integer value) {
        return isNotNull(value) ? value : 0;
    }

    public static Integer parseIntFromString(String value) {
        return CommonDataUtil.isNotEmpty(value) ? Integer.parseInt(value) : 0;
    }

    public static Map<String, Object> getAttributes(String jwtToken) {
        String[] split_string = jwtToken.split("\\.");
        String base64EncodedBody = split_string[1];

        org.apache.commons.codec.binary.Base64 base64Url = new Base64(true);

        String body = new String(base64Url.decode(base64EncodedBody));

        Map<String, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // convert JSON string to Map
            map = mapper.readValue(body, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Can't convert jsonString to mapObject");
        }
        return map;
    }

    public static String getSearchByValue(String columnName, String searchBy, String searchByValue) {
        return columnName.equals(searchBy) ? searchByValue : "";
    }

    public static String toPascalCase(String s, String delimiter) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        String[] words = s.split(delimiter);
        int wordsLength = words.length;
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < wordsLength; index++) {
            String word = words[index];
            sb.append(Character.toUpperCase(word.charAt(0)));
            sb.append(word.substring(1).toLowerCase());
            if (index != wordsLength - 1) {
                sb.append(delimiter);
            }
        }

        return sb.toString();
    }

    public static String toPascalCase(String s) {
        return toPascalCase(s, SPACE);
    }

    public static String evaluateJsonString(String str) {
        str = str.replaceAll("\"\\{", "\\{");
        str = str.replaceAll("\\}\"", "\\}");
        str = str.replaceAll("\\\\", "");
        str = str.replaceAll("\"\\[", "\\[");
        str = str.replaceAll("\\]\"", "\\]");
        str = str.replaceAll("\\\\", "");
        return str;
    }

    public static String getUserFullName(User user) {
        if (isNull(user)) {
            return DEFAULT_SYSTEM_USERNAME;
        }

        String firstName = Optional.ofNullable(user.getFirstName()).orElse("");
        String lastName = Optional.ofNullable(user.getLastName()).orElse("");
        if (isEmpty(firstName) && isEmpty(lastName)) {
            return DEFAULT_SYSTEM_USERNAME;
        }
        String fullName = String.join(SPACE, firstName, lastName).trim();
        return removeVietnameseAccents(fullName);
    }

    public static String removeVietnameseAccents(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
