package cn.wuxia.common.test;

import cn.wuxia.common.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

public class MainTest {

    public static void main(String[] args) {
        String fromHql = "From abc where absdfas =sdfasd fsdf-  fsdfs order By abc desc";
        int start = StringUtils.indexOfIgnoreCase(fromHql, "from ");
        int end =  StringUtils.indexOfIgnoreCase(fromHql, " order by ");
        System.out.println();
        System.out.println(start+" "+end);
    }
}
