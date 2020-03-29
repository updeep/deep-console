package cc.devcp.project.provider.utils;

import cc.devcp.project.provider.constant.Constants;

import static cc.devcp.project.common.constant.CommonConst.WORD_SEPARATOR;

/**
 * Content utils
 *
 * @author Nacos
 */
public class ContentUtils {

    public static void verifyIncrementPubContent(String content) {

        if (content == null || content.length() == 0) {
            throw new IllegalArgumentException("发布/删除内容不能为空");
        }
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\r' || c == '\n') {
                throw new IllegalArgumentException("发布/删除内容不能包含回车和换行");
            }
            if (c == Constants.WORD_SEPARATOR.charAt(0)) {
                throw new IllegalArgumentException("发布/删除内容不能包含(char)2");
            }
        }
    }

    public static String getContentIdentity(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("内容没有包含分隔符");
        }
        return content.substring(0, index);
    }

    public static String getContent(String content) {
        int index = content.indexOf(WORD_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("内容没有包含分隔符");
        }
        return content.substring(index + 1);
    }

    public static String truncateContent(String content) {
        if (content == null) {
            return "";
        } else if (content.length() <= LIMIT_CONTENT_SIZE) {
            return content;
        } else {
            return content.substring(0, 100) + "...";
        }
    }

    private final static int LIMIT_CONTENT_SIZE = 100;
}
