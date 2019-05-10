package cn.wuxia.common.spring.orm.core.jpa.repository.support;

import cn.wuxia.common.orm.PageSQLHandler;
import cn.wuxia.common.orm.query.Conditions;
import cn.wuxia.common.orm.query.Pages;
import cn.wuxia.common.orm.query.PropertyType;
import cn.wuxia.common.spring.orm.annotation.StateDelete;
import cn.wuxia.common.spring.orm.core.PropertyFilter;
import cn.wuxia.common.spring.orm.core.RestrictionNames;
import cn.wuxia.common.spring.orm.core.jpa.repository.BasicJpaRepository;
import cn.wuxia.common.spring.orm.core.jpa.specification.Specifications;
import cn.wuxia.common.spring.orm.enumeration.ExecuteMehtod;
import cn.wuxia.common.spring.orm.strategy.utils.ConvertCodeUtils;
import cn.wuxia.common.util.ArrayUtil;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.NumberUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.util.reflection.ConvertUtil;
import cn.wuxia.common.util.reflection.ReflectionUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * {@link BasicJpaRepository}接口实现类，并在{@link SimpleJpaRepository}基础上扩展,包含对
 * {@link PropertyFilter}的支持。或其他查询的支持, 重写了
 * {@link SimpleJpaRepository#save(Object)}和
 * {@link SimpleJpaRepository#delete(Object)}
 * 方法，支持@StateDelete注解和@ConvertProperty注解
 *
 * @param <T>  ORM对象
 * @param <ID> 主键Id类型
 * @author songlin.li
 */
public class JpaSupportRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BasicJpaRepository<T, ID> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    private EntityManager entityManager;

    private JpaEntityInformation<T, ?> entityInformation;

    protected Class<T> entityClass;

    public JpaSupportRepository(Class<T> domainClass, EntityManager entityManager) {
        super(domainClass, entityManager);
        this.entityManager = entityManager;
        // this.entityClass =
        // ReflectionUtil.getSuperClassGenricType(getClass());
        this.entityClass = (Class<T>) ReflectionUtil.getTargetClass(domainClass);
    }

    public JpaSupportRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityInformation = entityInformation;
        this.entityManager = entityManager;
        // this.entityClass =
        // ReflectionUtil.getSuperClassGenricType(getClass());
        this.entityClass = (Class<T>) ReflectionUtil.getTargetClass(entityInformation.getJavaType());
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public JpaEntityInformation<T, ?> getJpaEntityInformation() {
        return entityInformation;
    }


    @Override
    @Transactional
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "entity Can not be null");

        logger.debug("save entity {}", entityClass.getName());
        if (entityInformation.isNew(entity)) {
            ConvertCodeUtils.convertObject(entity, ExecuteMehtod.Save, ExecuteMehtod.Insert);
            entityManager.persist(entity);
            return entity;
        } else {
            ConvertCodeUtils.convertObject(entity, ExecuteMehtod.Save, ExecuteMehtod.Update);
            return entityManager.merge(entity);
        }
    }


    @Override
    @Transactional
    public void delete(T entity) {
        Assert.notNull(entity, "entity Can not be null");
        Class<?> entityClass = ReflectionUtil.getTargetClass(entity);
        logger.debug("delete entity {}", entityClass.getName());
        StateDelete stateDelete = ReflectionUtil.getAnnotation(entityClass, StateDelete.class);
        if (stateDelete != null) {
            Object value = ConvertUtil.convertToObject(stateDelete.value(), stateDelete.type().getValue());
            ReflectionUtil.invokeSetterMethod(entity, stateDelete.propertyName(), value);
            save(entity);
        } else {
            super.delete(entity);
        }
    }

    @Override
    public List<T> findBy(List<PropertyFilter> filters) {
        return findBy(filters, (Sort) null);
    }


    @Override
    public List<T> findBy(List<PropertyFilter> filters, Sort sort) {
        return findAll(Specifications.get(filters), sort);
    }


    @Override
    public Page<T> findPage(Pageable pageable, List<PropertyFilter> filters) {
        if (CollectionUtils.isEmpty(filters)) {
            return findAll(pageable);
        }
        Specification s = Specifications.get(filters);
        return findAll(Specifications.get(filters), pageable);
    }


    @Override
    public List<T> findBy(String propertyName, Object value) {
        return findBy(propertyName, value, (Sort) null);
    }


    @Override
    public List<T> findBy(String propertyName, Object value, Sort sort) {
        return findBy(propertyName, value, sort, RestrictionNames.EQ);
    }

    @Override
    public List<T> findBy(String propertyName, Object value, String restrictionName) {
        return findBy(propertyName, value, (Sort) null, restrictionName);
    }


    @Override
    public List<T> findBy(String propertyName, Object value, Sort sort, String restrictionName) {
        return findAll(Specifications.get(propertyName, value, restrictionName), sort);
    }


    @Override
    public T findOneBy(List<PropertyFilter> filters) {
        return (T) findOne(Specifications.get(filters)).get();
    }

    @Override
    public T findOneBy(String propertyName, Object value) {
        return findOneBy(propertyName, value, RestrictionNames.EQ);
    }

    @Override
    public T findOneBy(String propertyName, Object value, String restrictionName) {
        return (T) findOne(Specifications.get(propertyName, value, restrictionName)).orElse(null);
    }

    @Override
    public long count(PropertyFilter... filters) {
        return super.count(Specifications.get(filters));
    }

    @Override
    public Pages<T> findPage(Pages<T> pages) {

        List<Order> orders = Lists.newArrayList();
        if (null != pages.getSort()) {
            Iterator<cn.wuxia.common.orm.query.Sort.Order> it = pages.getSort().iterator();
            while (it.hasNext()) {
                cn.wuxia.common.orm.query.Sort.Order order = it.next();
                if (order.isAscending()) {
                    orders.add(new Order(Direction.ASC, order.getProperty()));
                } else {
                    orders.add(new Order(Direction.DESC, order.getProperty()));
                }
            }
        }
        List<PropertyFilter> filters = Lists.newArrayList();
        for (Conditions condition : pages.getConditions()) {
            PropertyFilter filter = new PropertyFilter();

            switch (condition.getMatchType()) {
                case LL:
                    filter.setRestrictionName(RestrictionNames.LLIKE);
                    break;
                case RL:
                    filter.setRestrictionName(RestrictionNames.RLIKE);
                    break;
                case EQ:
                    filter.setRestrictionName(RestrictionNames.EQ);
                    break;
                case NE:
                    filter.setRestrictionName(RestrictionNames.NE);
                    break;
                case BW:
                    break;
                case FL:
                    filter.setRestrictionName(RestrictionNames.LIKE);
                    break;
                case ISN:
                    filter.setRestrictionName(RestrictionNames.ISN);
                    break;
                case INN:
                    filter.setRestrictionName(RestrictionNames.INN);
                    break;
            }
            filter.setPropertyType(PropertyType.S.getValue());
            filter.setPropertyNames(new String[]{condition.getProperty()});
            filter.setMatchValue((String) condition.getValue());
            filters.add(filter);
        }

        if (pages.getPageSize() == -1) {
            List<T> result = findBy(filters, Sort.by(orders));
            pages.setResult(result);
            pages.setTotalCount(result.size());
        } else {
            PageRequest pageRequest = PageRequest.of(pages.getPageNo() - 1, pages.getPageSize(), Sort.by(orders));
            Page<T> page = findPage(pageRequest, filters);
            pages.setResult(page.getContent());
            pages.setPageNo(page.getNumber() + 1);
            pages.setPageSize(page.getSize());
            pages.setTotalCount(NumberUtil.toInteger(page.getTotalElements()));
        }
        return pages;
    }

    @Override
    public <X> Pages<X> queryPage(final Pages<X> page, final Class<X> clazz, final String jpql, final Object... values) {
        Assert.notNull(page, "page can not be null");
        /**
         * 动态拼接参数
         */
        List<Object> paramValue = ListUtil.arrayToList(values);
        String queryHql = PageSQLHandler.dualDynamicCondition(jpql, page.getConditions(), paramValue);
        if (page.isAutoCount()) {
            long totalCount = countHqlResult(queryHql, paramValue.toArray());
            page.setTotalCount(totalCount);
            if (totalCount == 0) {
                return page;
            }
        }

        queryHql += appendOrderBy(queryHql, page.getSort());

        Query query = createQuery(queryHql, clazz, paramValue.toArray());

        setPageParameterToQuery(query, page);

        List<X> result = query.getResultList();
        page.setResult(result);
        return page;
    }

    @Override
    public <X> Pages<X> queryPageBySQL(final Pages<X> page, final Class<X> clazz, final String sql, final Object... values) {

        /**
         * 动态拼接参数
         */
        List<Object> paramValue = ListUtil.arrayToList(values);
        String querySql = PageSQLHandler.dualDynamicCondition(sql, page.getConditions(), paramValue);
        if (page.isAutoCount()) {
            long totalCount = countSQLResult(querySql, paramValue.toArray());
            page.setTotalCount(totalCount);
            if (totalCount == 0) {
                return page;
            }
        }

        querySql += appendOrderBy(querySql, page.getSort());

        Query query = createSQLQuery(querySql, clazz, paramValue.toArray());

        setPageParameterToQuery(query, page);

        List<X> result = query.getResultList();
        page.setResult(result);

        return page;
    }


    /**
     * count record
     *
     * @param sql
     * @param values
     * @return
     * @author songlin.li
     */
    protected long countSQLResult(String sql, Object... values) {
        long recordTotal;
        int classNameIndex = sql.toLowerCase().indexOf("from");
        if (classNameIndex == -1) {
            return 0;
        } else {
            sql = "select count(1) as count from (" + sql + ") orgi";
        }

        Query query = createSQLQuery(sql, values);

        recordTotal = NumberUtil.toLong(query.getSingleResult(), 0L);
        logger.debug("Total: " + recordTotal);
        return recordTotal;
    }


    protected Query createSQLQuery(final String sql, final Object... values) {
        Assert.hasText(sql, "queryString can not be null");
        Query query = entityManager.createNativeQuery(sql);
        if (ArrayUtils.isNotEmpty(values)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    /**
                     * NativeQuery position start form 1 not 0
                     */
                    query.setParameter(i + 1, values[i]);
                }
            }
            logger.debug("values: {}", values);
        }
        return query;
    }


    /**
     * build the queryString to append the sort order by
     *
     * @param queryString
     * @param sort
     * @return
     * @author songlin
     */
    protected String appendOrderBy(String queryString, cn.wuxia.common.orm.query.Sort sort) {
        String orderBy = "";
        if (sort != null) {
            Assert.doesNotContain(queryString, "order by",
                    "duplicate order by,hql already has the sort: " + StringUtil.substringAfter(queryString, "order by"));
            orderBy = " order by " + sort.toString();
        }
        return orderBy;
    }

    protected Query createSQLQuery(final String sql, final Class clazz, final Object... values) {
        Assert.hasText(sql, "queryString can not be null");
        Query query = entityManager.createNativeQuery(sql, clazz);
        if (ArrayUtils.isNotEmpty(values)) {
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    /**
                     * NativeQuery position start form 1 not 0
                     */
                    query.setParameter(i + 1, values[i]);
                }
            }
            logger.debug("values: {}", values);
        }
        return query;
    }

    protected long countHqlResult(final String hql, final Object... values) {
        String countHql = prepareCountHql(hql);
        Object count = createQuery(countHql, values).getSingleResult();
        return NumberUtil.toLong(count, 0L);
    }


    protected Query createQuery(final String hql, final Object... values) {
        Assert.hasText(hql, "queryString can not be null");
        Query query = entityManager.createQuery(hql);
        if (ArrayUtil.isNotEmpty(values)) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    protected Query createQuery(final String hql, final Class clazz, final Object... values) {
        Assert.hasText(hql, "queryString can not be null");
        Query query = entityManager.createQuery(hql, clazz);
        if (ArrayUtil.isNotEmpty(values)) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    private String prepareCountHql(String orgHql) {
        String fromHql = orgHql;
        // the select clause and order by clause will affect the count query for
        // simple exclusion.
        fromHql = "from " + StringUtils.substringAfter(fromHql, "from");
        fromHql = StringUtils.substringBefore(fromHql, "order by");

        String countHql = "select count(*) " + fromHql;
        return countHql;
    }


    protected void setPageParameterToQuery(Query q, final Pages<?> page) {
        if (page.getPageSize() > 0) {
            // hibernate firstResult start with 0
            q.setFirstResult(page.getFirst() - 1);
            q.setMaxResults(page.getPageSize());
        }
    }
}
