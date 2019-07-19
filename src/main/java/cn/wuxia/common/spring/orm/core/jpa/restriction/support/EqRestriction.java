package cn.wuxia.common.spring.orm.core.jpa.restriction.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;

import cn.wuxia.common.spring.orm.core.MatchValue;
import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.restriction.PredicateSingleValueSupport;

/**
 * 等于约束 (from object o where o.value = ?) RestrictionName:EQ
 * <p>
 * 表达式:EQ属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author songlin.li
 */
public class EqRestriction extends PredicateSingleValueSupport {

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.restriction.PredicateSingleValueSupport
     * #getMatchValue(java.lang.String, java.lang.Class)
     */
    public MatchValue getMatchValue(String matchValue, Class<?> propertyType) {
        MatchValue matchValueModel = super.getMatchValue(matchValue, propertyType);
        for (int i = 0; i < matchValueModel.getValues().size(); i++) {
            Object value = matchValueModel.getValues().get(i);
            if (value instanceof String && StringUtils.equals(value.toString(), "null")) {
                matchValueModel.getValues().remove(i);
                matchValueModel.getValues().add(i, null);
            }
        }
        return matchValueModel;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#getRestrictionName
     * ()
     */
    @Override
    public String getRestrictionName() {
        return RestrictionNames.EQ;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.restriction.PredicateSingleValueSupport
     * #build(javax.persistence.criteria.Path, java.lang.Object,
     * javax.persistence.criteria.CriteriaBuilder)
     */
    public Predicate build(Path<?> expression, Object value, CriteriaBuilder builder) {

        return value == null ? builder.isNull(expression) : builder.equal(expression, value);
    }

}
