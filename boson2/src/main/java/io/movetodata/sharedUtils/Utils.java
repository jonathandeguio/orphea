package io.movetodata.sharedUtils;

import com.amazonaws.thirdparty.apache.codec.binary.Base64;
import io.movetodata.bob.library.models.SocketMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Utils {

    @Autowired
    SimpMessagingTemplate template;

    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static boolean isBase64(String value) {
        try {
            Base64.decodeBase64(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private final static Pattern UUID_REGEX_PATTERN =
            Pattern.compile("^[{]?[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}[}]?$");

    public static boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return UUID_REGEX_PATTERN.matcher(str).matches();
    }

    public boolean sendSocket(String topic, String message) {
        SocketMessage textMessage = new SocketMessage();
        textMessage.setMessage("active");
        template.convertAndSend(topic, message);

        return true;
    }

    public static boolean isPostgresTableNameValid(String str) {
        final String VALID_PATTERN = "^[a-zA-Z_][a-zA-Z0-9 _-]*$";
        final Pattern pattern = Pattern.compile(VALID_PATTERN);

        return pattern.matcher(str).matches();
    }

    public static boolean isSparkInKubernetes() {
        return Objects.equals(System.getenv("SPARK_TYPE"), "kubernetes");
    }
}
