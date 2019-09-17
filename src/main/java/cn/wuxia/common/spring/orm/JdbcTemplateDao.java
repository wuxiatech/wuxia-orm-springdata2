package cn.wuxia.common.spring.orm;

import cn.wuxia.common.orm.PageSQLHandler;
import cn.wuxia.common.orm.query.Pages;
import cn.wuxia.common.orm.query.Sort;
import cn.wuxia.common.util.ListUtil;
import cn.wuxia.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.List;

public abstract class JdbcTemplateDao {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected JdbcTemplate jdbcTemplate;

    protected abstract void setJdbcTemplate(final DataSource dataSource);

    /**
     * update by sql, jdbcTemplate mode
     *
     * @param sql
     * @param objs
     */
    protected void saveOrUpdate(String sql, Object... objs) {
        jdbcTemplate.update(sql, objs);
    }

    /**
     * 支持简单的Conditions 赋值查询，复杂sql请自行处理再调用本方法
     *
     * @param page
     * @param clas
     * @param sql
     * @param values
     * @return
     * @author songlin
     */
    protected <X> Pages<X> findPageBySql(final Pages<X> page, final Class<X> clas, final String sql, final Object... values) {
        int classNameIndex = sql.toLowerCase().indexOf("from");
        if (classNameIndex == -1)
            return null;
        Assert.notNull(page, "page can not be null");
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
        querySql += " limit ?,?";
        paramValue.add(page.getFirst() - 1);
        paramValue.add(page.getPageSize());
        logger.debug(querySql);
        logger.debug("{}", paramValue);
        List<X> list = jdbcTemplate.query(querySql, BeanPropertyRowMapper.newInstance(clas), paramValue.toArray());
        page.setResult(list);
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
        if (classNameIndex == -1)
            return 0;
        else {
            sql = "select count(1) as count from (" + sql + ") orgi";
        }
        recordTotal = jdbcTemplate.queryForObject(sql, Long.class, values);
        logger.debug("Total: " + recordTotal);
        return recordTotal;
    }


    /**
     * build the queryString to append the sort order by
     *
     * @param queryString
     * @param sort
     * @return
     * @author songlin
     */
    protected String appendOrderBy(String queryString, Sort sort) {
        String orderBy = "";
        if (sort != null) {
            Assert.doesNotContain(queryString, "order by",
                    "duplicate order by,hql already has the sort: " + StringUtil.substringAfter(queryString, "order by"));
            orderBy = " order by " + sort.toString();
        }
        return orderBy;
    }

}
