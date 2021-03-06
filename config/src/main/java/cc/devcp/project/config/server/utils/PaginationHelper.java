package cc.devcp.project.config.server.utils;

import cc.devcp.project.config.server.model.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static cc.devcp.project.core.utils.SystemUtils.STANDALONE_MODE;

/**
 * 分页辅助类
 *
 * @param <E>
 * @author boyan
 * @date 2010-5-6
 */
public class PaginationHelper<E> {

    /**
     * 取分页
     *
     * @param jt           jdbcTemplate
     * @param sqlCountRows 查询总数的SQL
     * @param sqlFetchRows 查询数据的sql
     * @param args         查询参数
     * @param pageNo       页数
     * @param pageSize     每页大小
     * @param rowMapper
     * @return
     */
    public Page<E> fetchPage(final JdbcTemplate jt, final String sqlCountRows, final String sqlFetchRows,
                             final Object[] args, final int pageNo, final int pageSize, final RowMapper<E> rowMapper) {
        return fetchPage(jt, sqlCountRows, sqlFetchRows, args, pageNo, pageSize, null, rowMapper);
    }

    public Page<E> fetchPage(final JdbcTemplate jt, final String sqlCountRows, final String sqlFetchRows,
                             final Object[] args, final int pageNo, final int pageSize, final Long lastMaxId,
                             final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }

        // 查询当前记录总数
        Integer rowCountInt = jt.queryForObject(sqlCountRows, Integer.class, args);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);

        if (pageNo > pageCount) {
            return page;
        }

        final int startRow = (pageNo - 1) * pageSize;
        String selectSQL = "";
        if (PropertyUtil.isStaticUseDerby()) {
            selectSQL = sqlFetchRows + " OFFSET " + startRow + " ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        } else if (lastMaxId != null) {
            selectSQL = sqlFetchRows + " and id > " + lastMaxId + " order by id asc" + " limit " + 0 + "," + pageSize;
        } else {
            selectSQL = sqlFetchRows + " limit " + startRow + "," + pageSize;
        }

        List<E> result = jt.query(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public Page<E> fetchPageLimit(final JdbcTemplate jt, final String sqlCountRows, final String sqlFetchRows,
                                  final Object[] args, final int pageNo, final int pageSize,
                                  final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 查询当前记录总数
        Integer rowCountInt = jt.queryForObject(sqlCountRows, Integer.class);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);

        if (pageNo > pageCount) {
            return page;
        }

        String selectSQL = sqlFetchRows;
        if (PropertyUtil.isStaticUseDerby()) {
            selectSQL = selectSQL.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        List<E> result = jt.query(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public Page<E> fetchPageLimit(final JdbcTemplate jt, final String sqlCountRows, final Object[] args1,
                                  final String sqlFetchRows,
                                  final Object[] args2, final int pageNo, final int pageSize,
                                  final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 查询当前记录总数
        Integer rowCountInt = jt.queryForObject(sqlCountRows, Integer.class, args1);
        if (rowCountInt == null) {
            throw new IllegalArgumentException("fetchPageLimit error");
        }

        // 计算页数
        int pageCount = rowCountInt / pageSize;
        if (rowCountInt > pageSize * pageCount) {
            pageCount++;
        }

        // 创建Page对象
        final Page<E> page = new Page<E>();
        page.setPageNumber(pageNo);
        page.setPagesAvailable(pageCount);
        page.setTotalCount(rowCountInt);

        if (pageNo > pageCount) {
            return page;
        }

        String selectSQL = sqlFetchRows;
        if (PropertyUtil.isStaticUseDerby()) {
            selectSQL = selectSQL.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        List<E> result = jt.query(selectSQL, args2, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public Page<E> fetchPageLimit(final JdbcTemplate jt, final String sqlFetchRows,
                                  final Object[] args, final int pageNo, final int pageSize,
                                  final RowMapper<E> rowMapper) {
        if (pageNo <= 0 || pageSize <= 0) {
            throw new IllegalArgumentException("pageNo and pageSize must be greater than zero");
        }
        // 创建Page对象
        final Page<E> page = new Page<E>();

        String selectSQL = sqlFetchRows;
        if (PropertyUtil.isStaticUseDerby()) {
            selectSQL = selectSQL.replaceAll("(?i)LIMIT \\?,\\?", "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        List<E> result = jt.query(selectSQL, args, rowMapper);
        for (E item : result) {
            page.getPageItems().add(item);
        }
        return page;
    }

    public void updateLimit(final JdbcTemplate jt, final String sql, final Object[] args) {
        String sqlUpdate = sql;

        if (PropertyUtil.isStaticUseDerby()) {
            sqlUpdate = sqlUpdate.replaceAll("limit \\?", "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY");
        }

        jt.update(sqlUpdate, args);
    }
}
