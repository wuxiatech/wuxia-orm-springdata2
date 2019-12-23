package cn.wuxia.common.spring.orm.core.jpa.specification;

import java.util.List;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import cn.wuxia.common.orm.query.Conditions;
import cn.wuxia.common.spring.orm.core.jpa.specification.support.ConditionsSpecification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import cn.wuxia.common.spring.orm.core.PropertyFilter;
import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.specification.support.PropertyFilterSpecification;
import cn.wuxia.common.spring.orm.core.jpa.specification.support.PropertySpecification;

/**
 * Specification工具类,帮助通过PropertyFilter和属性名创建Specification
 * 
 * @author songlin.li
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Specifications {


    /**
     * 通过属性过滤器集合，创建Specification
     *
     * @param filters 属性过滤器集合
     * @return {@link Specification}
     */
    public static Specification get(PropertyFilter... filters) {
        return new PropertyFilterSpecification(filters);
    }
    /**
     * 通过属性过滤器，创建Specification
     * 
     * @param filter 属性过滤器
     * @return {@link Specification}
     */
    public static Specification get(PropertyFilter filter) {
        return new PropertyFilterSpecification(filter);
    }


    /**
     * 通过属性过滤器集合，创建Specification
     *
     * @param conditions 属性过滤器集合
     * @return {@link Specification}
     */
    public static Specification get(Conditions... conditions) {
        return new ConditionsSpecification(conditions);
    }
    /**
     * 通过属性过滤器，创建Specification
     *
     * @param condition 属性过滤器
     * @return {@link Specification}
     */
    public static Specification get(Conditions condition) {
        return new ConditionsSpecification(condition);
    }

    /**
     * 通过类属性名称，创建Specification
     * 
     * @param propertyName 属性名
     * @param value 值
     * @return {@link Specification}
     */
    public static Specification get(String propertyName, Object value) {
        return get(propertyName, value, RestrictionNames.EQ);
    }

    /**
     * 通过类属性名称，创建Specification
     * 
     * @param propertyName 属性名
     * @param value 值
     * @param restrictionName 约束条件名称，{@link RestrictionNames}
     * @return {@link Specification}
     */
    public static Specification get(String propertyName, Object value, String restrictionName) {
        return new PropertySpecification(propertyName, value, restrictionName);
    }

    /**
     * 获取属性名字路径
     * 
     * @param propertyName 属性名
     * @param root Query roots always reference entities
     * @return {@link Path}
     */
    public static Path<?> getPath(String propertyName, Root<?> root) {

        Path<?> path = null;

        if (StringUtils.contains(propertyName, ".")) {
            String[] propertys = StringUtils.splitByWholeSeparator(propertyName, ".");
            path = root.get(propertys[0]);
            for (int i = 1; i < propertys.length; i++) {
                path = path.get(propertys[i]);
            }
        } else {
            path = root.get(propertyName);
        }

        return path;
    }

}
