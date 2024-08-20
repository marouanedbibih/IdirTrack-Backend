package com.idirtrack.backend.basics;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;


public class BasicValidation {
    public static Map<String, String> getValidationsErrors(BindingResult bindingResult) {
        Map<String, String> errorsMap = new HashMap<>();

        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                if (!errorsMap.containsKey(error.getField())) {
                    errorsMap.put(error.getField(), error.getDefaultMessage());
                }
            }
        }

        return errorsMap;
    }
}
// public class BasicValidation {
//     public static Map<String, String> getValidationsErrors(BindingResult bindingResult) {
//         Map<String, String> errorsMap = new HashMap<>();

//         if (bindingResult.hasErrors()) {
//             for (FieldError error : bindingResult.getFieldErrors()) {
//                 if (!errorsMap.containsKey(error.getField())) {
//                     errorsMap.put(error.getField(), error.getDefaultMessage());
//                 }
//             }
//         }

//         return errorsMap;
//     }
// }


