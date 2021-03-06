package cc.devcp.project.config.server.service;

import cc.devcp.project.config.server.constant.Constants;
import cc.devcp.project.config.server.utils.LogUtil;
import cc.devcp.project.config.server.utils.PropertyUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static cc.devcp.project.core.utils.SystemUtils.*;

/**
 * local data source
 *
 * @author Nacos
 */
@Service("localDataSourceService")
public class LocalDataSourceServiceImpl implements DataSourceService {

    private static final Logger logger = LoggerFactory.getLogger(LocalDataSourceServiceImpl.class);

    private static final String JDBC_DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String DERBY_BASE_DIR = "data" + File.separator + "derby-data";
    private static final String DERBY_USERNAME = "deep";
    private static final String DERBY_PASSWORD = "deep";

    @Autowired
    private PropertyUtil propertyUtil;

    private JdbcTemplate jt;
    private TransactionTemplate tjt;

    @PostConstruct
    public void init() {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(JDBC_DRIVER_NAME);
        ds.setJdbcUrl("jdbc:derby:" + APP_HOME + File.separator + DERBY_BASE_DIR + ";create=true");
        ds.setUsername(DERBY_USERNAME);
        ds.setPassword(DERBY_PASSWORD);
        // 连接只读数据库时配置为true， 保证安全
        ds.setReadOnly(false);
        // 等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生SQLException， 缺省:30秒
        ds.setConnectionTimeout(30000);
        // 一个连接idle状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟
        ds.setIdleTimeout(600000);
        // 一个连接的生命时长（毫秒），超时而且没被使用则被释放（retired），缺省:30分钟，建议设置比数据库超时时长少30秒，
        // 参考MySQL wait_timeout参数（show variables like '%timeout%';）
        ds.setMaxLifetime(1800000);
        // 连接池中允许的最大连接数。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count)
        ds.setMaximumPoolSize(15);

        jt = new JdbcTemplate();
        jt.setMaxRows(50000);
        jt.setQueryTimeout(5000);
        jt.setDataSource(ds);
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tjt = new TransactionTemplate(tm);
        tm.setDataSource(ds);
        tjt.setTimeout(5000);

        if (STANDALONE_MODE && !propertyUtil.isStandaloneUseMysql()) {
            reload();
        }
    }

    @Override
    public void reload() {
        DataSource ds = jt.getDataSource();
        if (ds == null) {
            throw new RuntimeException("datasource is null");
        }
        try {
            execute(ds.getConnection(), "META-INF/db-derby-schema.sql");
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
            throw new RuntimeException("load db-derby-schema.sql error." + e);
        }
    }

    @Override
    public boolean checkMasterWritable() {
        return true;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jt;
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return tjt;
    }

    @Override
    public String getCurrentDBUrl() {
        return "jdbc:derby:" + APP_HOME + File.separator + DERBY_BASE_DIR + ";create=true";
    }

    @Override
    public String getHealth() {
        return "UP";
    }

    /**
     * 读取SQL文件
     *
     * @param sqlFile sql
     * @return sqls
     * @throws Exception Exception
     */
    private List<String> loadSql(String sqlFile) throws Exception {
        List<String> sqlList = new ArrayList<String>();
        InputStream sqlFileIn = null;
        try {
            if (StringUtils.isBlank(System.getProperty(APP_HOME_KEY))) {
                ClassLoader classLoader = getClass().getClassLoader();
                URL url = classLoader.getResource(sqlFile);
                sqlFileIn = url.openStream();
            } else {
                File file = new File(
                    System.getProperty(APP_HOME_KEY) + File.separator + "conf" + File.separator + "db-derby-schema.sql");
                sqlFileIn = new FileInputStream(file);
            }

            StringBuilder sqlSb = new StringBuilder();
            byte[] buff = new byte[1024];
            int byteRead = 0;
            while ((byteRead = sqlFileIn.read(buff)) != -1) {
                sqlSb.append(new String(buff, 0, byteRead, Constants.ENCODE));
            }

            String[] sqlArr = sqlSb.toString().split(";");
            for (int i = 0; i < sqlArr.length; i++) {
                String sql = sqlArr[i].replaceAll("--.*", "").trim();
                if (StringUtils.isNotEmpty(sql)) {
                    sqlList.add(sql);
                }
            }
            return sqlList;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            if (sqlFileIn != null) {
                sqlFileIn.close();
            }
        }
    }

    /**
     * 执行SQL语句
     *
     * @param conn    connect
     * @param sqlFile sql
     * @throws Exception Exception
     */
    private void execute(Connection conn, String sqlFile) throws Exception {
        Statement stmt = null;
        try {
            List<String> sqlList = loadSql(sqlFile);
            stmt = conn.createStatement();
            for (String sql : sqlList) {
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    LogUtil.defaultLog.info(e.getMessage());
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

}
