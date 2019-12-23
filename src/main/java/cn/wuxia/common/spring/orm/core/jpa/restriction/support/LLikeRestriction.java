package cn.wuxia.common.spring.orm.core.jpa.restriction.support;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.restriction.PredicateSingleValueSupport;

/**
 * 左模糊约束 ( from object o where o.value like '?%') RestrictionName:LLIKE
 * <p>
 * 表达式:LLIKE属性类型_属性名称[_OR_属性名称...]
 * </p>
 * 
 * @author songlin.li
 */
public class LLikeRestriction extends PredicateSingleValueSupport {

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#getRestrictionName
     * ()
     */
    @Override
    public String getRestrictionName() {
        return RestrictionNames.LLIKE;
    }

    /*
     * (non-Javadoc)
     * @see org.exitsoft.orm.core.spring.data.jpa.PredicateBuilder#build(javax.
     * persistence.criteria.Path, java.lang.Object,
     * javax.persistence.criteria.CriteriaBuilder)
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Predicate build(Path expression, Object value, CriteriaBuilder builder) {

        return builder.like(expression,  value+"%");
    }

}
