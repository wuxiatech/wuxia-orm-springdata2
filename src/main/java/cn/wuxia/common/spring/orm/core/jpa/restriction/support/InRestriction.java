package cn.wuxia.common.spring.orm.core.jpa.restriction.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.restriction.PredicateMultipleValueSupport;

/**
 * 包含约束 (from object o where o.value in (?,?,?,?,?))RestrictionName:IN
 * <p>
 * 表达式:IN属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author songlin.li
 */
public class InRestriction extends PredicateMultipleValueSupport {

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#getRestrictionName
     * ()
     */
    @Override
    public String getRestrictionName() {
        return RestrictionNames.IN;
    }

    /*
     * (non-Javadoc)
     * @see org.exitsoft.orm.core.spring.data.jpa.restriction.
     * PredicateMultipleValueSupport
     * #buildRestriction(javax.persistence.criteria.Path, java.lang.Object[],
     * javax.persistence.criteria.CriteriaBuilder)
     */
    @SuppressWarnings("rawtypes")
    public Predicate buildRestriction(Path expression, Object[] values, CriteriaBuilder builder) {
        return expression.in(values);
    }

}
