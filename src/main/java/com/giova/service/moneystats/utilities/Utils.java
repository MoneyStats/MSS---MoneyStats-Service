package com.giova.service.moneystats.utilities;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.util.ObjectUtils;

public class Utils {

  public static boolean checkCharacterAndRegexValid(String field, String regex) {
    if (ObjectUtils.isEmpty(field) || ObjectUtils.isEmpty(regex)) return false;
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(field);
    return m.find();
  }

  public static <T> boolean isNullOrEmpty(T obj) {
    return switch (obj) {
      case null -> true; // L'oggetto è null
      case String s -> s.isEmpty(); // Se è una stringa, verifica se è vuota
      case Collection<?> collection ->
          collection.isEmpty(); // Se è una collezione (List, Set), verifica se è vuota
      case Map<?, ?> map -> map.isEmpty(); // Se è una mappa, verifica se è vuota
      default -> false;
    };
  }
}
