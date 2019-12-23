package cn.wuxia.common.spring.orm.core.jpa.restriction;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import cn.wuxia.common.spring.orm.core.PropertyFilter;
import cn.wuxia.common.spring.orm.core.jpa.specification.SpecificationModel;
import cn.wuxia.common.util.reflection.ConvertUtil;

/**
 * 对{@link PropertyFilter#getMatchValue()}的特殊情况值做处理，例如 in, not in, between的多值情况,
 * 该类值处理一种情况
 * <p>
 * 例如:
 * </p>
 * INI_property = "1,2,3,4";
 * <p>
 * 会产生的sql为: property in (1,2,3,4)
 * 
 * @author songlin.li
 */
public abstract class PredicateMultipleValueSupport extends PredicateSingleValueSupport {

    /**
     * 将得到值与指定分割符号,分割,得到数组
     * 
     * @param value 值
     * @param type 值类型
     * @return Object
     */
    public Object convertMatchValue(String value, Class<?> type) {
        Assert.notNull(value, "值不能为空");
        String[] result = StringUtils.splitByWholeSeparator(value, getAndValueSeparator());

        return ConvertUtil.convertToObject(result, type);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.restriction.PredicateSingleValueSupport
     * #build(org.exitsoft.orm.core.PropertyFilter,
     * org.exitsoft.orm.core.spring.data.jpa.JpaBuilderModel)
     */
    @Override
    public Predicate build(PropertyFilter filter, SpecificationModel model) {
        Object value = convertMatchValue(filter.getMatchValue(), filter.getPropertyType());
        Predicate predicate = null;

        if (filter.hasMultiplePropertyNames()) {
            Predicate orDisjunction = model.getBuilder().disjunction();
            for (String propertyName : filter.getPropertyNames()) {
                orDisjunction.getExpressions().add(build(propertyName, value, model));
            }
            predicate = orDisjunction;
        } else {
            predicate = build(filter.getSinglePropertyName(), value, model);
        }

        return predicate;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.exitsoft.orm.core.spring.data.jpa.restriction.PredicateSingleValueSupport
     * #build(javax.persistence.criteria.Path, java.lang.Object,
     * javax.persistence.criteria.CriteriaBuilder)
     */
    @Override
    public Predicate build(Path<?> expression, Object value, CriteriaBuilder builder) {
        return buildRestriction(expression, (Object[]) value, builder);
    }

    /**
     * 获取Jpa的约束标准
     * 
     * @param expression root路径
     * @param values 值
     * @param builder CriteriaBuilder
     * @return {@link Predicate}
     */
    public abstract Predicate buildRestriction(Path<?> expression, Object[] values, CriteriaBuilder builder);
}
