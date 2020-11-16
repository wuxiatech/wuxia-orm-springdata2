package cn.wuxia.common.spring.orm.mongo;

import cn.wuxia.common.orm.query.Conditions;
import cn.wuxia.common.orm.query.MatchType;
import cn.wuxia.common.orm.query.Pages;
import cn.wuxia.common.orm.query.Sort.Order;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import cn.wuxia.common.util.reflection.ReflectionUtil;
import com.google.common.collect.Lists;
import com.mongodb.client.result.DeleteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public abstract class SpringDataMongoDao<T, K extends Serializable> {
    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected MongoTemplate mongoTemplate;

    /**
     * 如非实体存储的时候需要制定collection name
     *
     * @return
     */
    protected String collectionName;

    public SpringDataMongoDao() {
    }

    public SpringDataMongoDao(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * @param query
     * @return
     */
    public List<T> find(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().find(query, this.getEntityClass());
        } else {
            return getMongoTemplate().find(query, this.getEntityClass(), collectionName);
        }
    }

    /**
     * 查找唯一
     *
     * @param query
     * @return
     */
    public T findUnique(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().findOne(query, this.getEntityClass());
        } else {
            return getMongoTemplate().findOne(query, this.getEntityClass(), collectionName);
        }
    }

    /**
     * @param properties
     * @param value
     * @return
     */
    public T findUniqueBy(final String properties, final Object value) {
        Query query = new Query(Criteria.where(properties).is(value));
        return this.findUnique(query);
    }

    /**
     * @param conditions
     * @return
     */
    public T findUniqueBy(final Conditions... conditions) {
        return this.findUnique(condition2Query(conditions));
    }

    /**
     * @param properties
     * @param value
     * @return
     */
    public List<T> findIn(final String properties, final Object... value) {
        Query query = new Query(Criteria.where(properties).in(value));
        return this.find(query);
    }

    /**
     * 更新
     *
     * @param query
     * @param update
     */
    public void update(Query query, Update update) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().updateMulti(query, update, this.getEntityClass());
        } else {
            getMongoTemplate().updateMulti(query, update, this.getEntityClass(), collectionName);
        }
    }

    /**
     * 更新
     *
     * @param query
     * @param update
     */
    public T updateFirst(Query query, Update update) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().findAndModify(query, update, this.getEntityClass());
        } else {
            return getMongoTemplate().findAndModify(query, update, this.getEntityClass(), collectionName);
        }
    }

    /**
     * 删除
     *
     * @param query
     */
    public void delete(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().remove(query, this.getEntityClass());
        } else {
            getMongoTemplate().remove(query, this.getEntityClass(), collectionName);
        }
    }

    /**
     * 保存
     *
     * @param entity
     * @return
     */
    public void save(T entity) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().insert(entity);
        } else {
            getMongoTemplate().insert(entity, collectionName);
        }
    }

    public void batchSave(Collection<T> entitys) {
        if (ListUtil.isEmpty(entitys)) {
            return;
        }
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().insert(entitys, this.getEntityClass());
        } else {
            getMongoTemplate().insert(entitys, collectionName);
        }
    }

    public void save(Map<String, ?> m) {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().insert(m);
        } else {
            getMongoTemplate().insert(m, collectionName);
        }
    }

    /**
     * 删除对象
     *
     * @param entity
     * @author songlin
     */
    public void delete(T entity) {
        if (StringUtil.isBlank(collectionName)) {
            DeleteResult res = getMongoTemplate().remove(entity);
            logger.info("", res);
        } else {
            DeleteResult res = getMongoTemplate().remove(entity, collectionName);
            logger.info("", res);
        }
    }

    public void deleteById(final K id) throws Exception {
        if (StringUtil.isBlank(collectionName)) {
            getMongoTemplate().remove(new Query().addCriteria(Criteria.where(getIdName()).is(id)), this.getEntityClass());
        } else {
            getMongoTemplate().remove(new Query().addCriteria(Criteria.where(getIdName()).is(id)), this.getEntityClass(), collectionName);
        }
    }

    /**
     * 根据id查找
     *
     * @param id
     * @return
     */
    public T findById(final K id) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().findById(id, this.getEntityClass());
        } else {
            return getMongoTemplate().findById(id, this.getEntityClass(), collectionName);
        }
    }

    /**
     * 根据某个熟悉查找
     *
     * @param properties
     * @param value
     * @return
     */
    public List<T> findBy(final String properties, final Object value) {
        Query query = new Query(Criteria.where(properties).is(value));
        return this.find(query);
    }

    /**
     * 根据某个熟悉查找
     *
     * @param properties
     * @param value
     * @return
     */
    public List<T> findBy(final String properties, final Object value, String orderby, Direction direction) {
        Query query = new Query(Criteria.where(properties).is(value));
        Sort sort =  Sort.by(direction, orderby);
        query.with(sort);
        return this.find(query);
    }

    /**
     * 分页查找
     *
     * @param page
     * @return
     */
    public Pages<T> findPage(Pages<T> page) {
        return findPage(new Query(), page);
    }

    /**
     * 分页查找
     *
     * @param query
     * @param page
     * @return
     */
    public Pages<T> findPage(Query query, Pages<T> page) {
        addCondition2Query(query, page.getConditions());
        long count = this.count(query);
        if (count <= 0) {
            return page;
        }
        page.setTotalCount(count);
        int pageNumber = page.getPageNo();
        int pageSize = page.getPageSize();
        if (pageSize != -1) {
            query.skip((pageNumber - 1) * pageSize).limit(pageSize);
        }
        if (page.getSort() != null) {
            Iterator<Order> iterator = page.getSort().iterator();
            List<Sort.Order> orders = Lists.newLinkedList();
            while (iterator.hasNext()) {
                Order order = iterator.next();
                if (order.isAscending()) {
                    orders.add(new Sort.Order(Direction.ASC, order.getProperty()));
                } else {
                    orders.add(new Sort.Order(Direction.DESC, order.getProperty()));
                }
            }
            query.with(Sort.by(orders));
        }
        List<T> rows = this.find(query);
        page.setResult(rows);
        return page;
    }

    /**
     * 分页查找
     *
     * @param query
     * @param conditions
     * @return
     */
    protected void addCondition2Query(Query query, List<Conditions> conditions) {
        addCondition2Query(query, conditions.toArray(new Conditions[]{}));
    }

    /**
     * 分页查找
     *
     * @param query
     * @param conditions
     * @return
     */
    protected void addCondition2Query(Query query, Conditions... conditions) {
        for (Conditions cond : conditions) {
            /**
             * 除了is null or is not null 条件外，其他条件必须带值
             */
            if (cond.getMatchType() != MatchType.ISN && cond.getMatchType() != MatchType.INN) {
                if (StringUtil.isBlank(cond.getValue())) {
                    logger.warn("condition: " + cond.getProperty() + " value is null, ignore this condition");
                    continue;
                }
            }
            switch (cond.getMatchType()) {

                case EQ:
                    query.addCriteria(Criteria.where(cond.getProperty()).is(cond.getValue()));
                    break;
                case NE:
                    query.addCriteria(Criteria.where(cond.getProperty()).ne(cond.getValue()));
                    break;
                case ISN:
                    query.addCriteria(Criteria.where(cond.getProperty()).is(null));
                    break;
                case INN:
                    query.addCriteria(Criteria.where(cond.getProperty()).ne(null));
                    break;
                case LL:
                    //左匹配
                    query.addCriteria(
                            Criteria.where(cond.getProperty()).regex(Pattern.compile("^.\"+cond.getValue()+\"*$", Pattern.CASE_INSENSITIVE)));
                    break;
                case RL:
                    //右匹配
                    query.addCriteria(
                            Criteria.where(cond.getProperty()).regex(Pattern.compile("^.*\"+cond.getValue()+\"$", Pattern.CASE_INSENSITIVE)));
                    break;
                case FL:
                    //模糊匹配
                    query.addCriteria(
                            Criteria.where(cond.getProperty()).regex(Pattern.compile("^.*" + cond.getValue() + ".*$", Pattern.CASE_INSENSITIVE)));
                    break;
                case NL:
                    break;
                case LT:
                    query.addCriteria(Criteria.where(cond.getProperty()).lt(cond.getValue()));
                    break;
                case GT:
                    query.addCriteria(Criteria.where(cond.getProperty()).gt(cond.getValue()));
                    break;
                case GTE:
                    query.addCriteria(Criteria.where(cond.getProperty()).gte(cond.getValue()));
                    break;
                case LTE:
                    query.addCriteria(Criteria.where(cond.getProperty()).lte(cond.getValue()));
                    break;
                case IN:
                    query.addCriteria(Criteria.where(cond.getProperty()).in(cond.getValue()));
                    break;
                case NIN:
                    query.addCriteria(Criteria.where(cond.getProperty()).nin(cond.getValue()));
                    break;
                case BW:
                    query.addCriteria(Criteria.where(cond.getProperty()).gte(cond.getValue()).lte(cond.getAnotherValue()));
                    break;
                default:
                    break;
            }

        }
    }

    /**
     * 将condition转换为mongodb query
     *
     * @param conditions
     * @return
     */
    protected Query condition2Query(Conditions... conditions) {
        Query query = new Query();
        addCondition2Query(query, conditions);
        return query;
    }
    /**
     * 统计总数
     *
     * @param query
     * @return
     */
    protected long count(Query query) {
        if (StringUtil.isBlank(collectionName)) {
            return getMongoTemplate().count(query, this.getEntityClass());
        } else {
            return getMongoTemplate().count(query, this.getEntityClass(), collectionName);
        }
    }

    /**
     * 获取需要操作的实体类class
     *
     * @return
     */
    protected Class<T> getEntityClass() {
        return ReflectionUtil.getSuperClassGenricType(getClass());
    }

    /**
     * 可以重写注入
     *
     * @param mongoTemplate
     */
    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getIdName() throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(this.getEntityClass());
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

        String idName = null;
        for (int i = 0; i < properties.length; i++) {
            Method get = properties[i].getReadMethod();
            if (get.getAnnotation(Id.class) == null) {
                continue;
            }
            idName = properties[i].getName();
            break;
        }
        return idName;
    }
}
