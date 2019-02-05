package io.github.qyvlik.matchengine.core.durable.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;

public class MatchEngineDBFactory {

    public static final Set<String> BLACK_SYMBOL_NAMES =
            new TreeSet<String>(Lists.newArrayList("sys"));

    public static final String SYMBOL_PREFIX = "symbol:";

    private final Map<String, DB> dbMap = Maps.newConcurrentMap();
    private Logger logger = LoggerFactory.getLogger(getClass());
    private DB sysDB;

    private String diskDirectory;
    private Integer dBDiskLimit;

    public MatchEngineDBFactory(String diskDirectory, Integer dBDiskLimit) {
        this.diskDirectory = diskDirectory;
        this.dBDiskLimit = dBDiskLimit;
    }

    public String getDiskDirectory() {
        return diskDirectory;
    }

    public Integer getBDiskLimit() {
        return dBDiskLimit;
    }

    public DB getSysDB() {
        return sysDB;
    }

    public Map<String, DB> getDbMap() {
        return dbMap;
    }

    public DB createDBBySymbol(String symbol, boolean createIfMissing) {
        // not include `sys`
        if (getBDiskLimit() < dbMap.size() - 1) {
            throw new RuntimeException("createDBBySymbol failure : group count more than "
                    + dBDiskLimit);
        }

        return createDBBySymbol(symbol, createIfMissing, false);
    }

    protected DB createDBBySymbol(String group, boolean createIfMissing, boolean ignoreBlackList) {
        if (createIfMissing) {
            return dbMap.computeIfAbsent(group, k -> createSymbolBucketInternal(group, ignoreBlackList));
        } else {
            return dbMap.get(group);
        }
    }

    private DB createSymbolBucketInternal(String group, boolean ignoreBlackList) {
        if (StringUtils.isBlank(group)) {
            throw new RuntimeException("createDBBySymbol failure : group was empty");
        }

        if (!ignoreBlackList && BLACK_SYMBOL_NAMES.contains(group)) {
            throw new RuntimeException("createDBBySymbol failure : group "
                    + group + " is in blacklist");
        }

        if (group.contains("/")) {
            throw new RuntimeException("createDBBySymbol failure : group "
                    + group + " contains invalidate character");
        }

        if (StringUtils.isBlank(getDiskDirectory())) {
            throw new RuntimeException("createDBBySymbol failure : diskDirectory was empty");
        }

        DB db = null;

        Options options = new Options();
        options.createIfMissing(true);

        String directory = getDiskDirectory();

        String symbolDirectory;

        if (directory.endsWith("/")) {
            symbolDirectory = directory + group;
        } else {
            symbolDirectory = directory + "/" + group;
        }

        if (!BLACK_SYMBOL_NAMES.contains(group)) {
            sysDB.put(bytes(SYMBOL_PREFIX + group), bytes(group));           // save group
        }

        try {
            db = factory.open(new File(symbolDirectory), options);
        } catch (Exception e) {
            logger.error("create leveldb failure:", e);
            throw new RuntimeException(e);
        }

        return db;
    }

    @PostConstruct
    public void loadDBs() throws Exception {
        sysDB = createDBBySymbol("sys", true, true);

        DBIterator iterator = sysDB.iterator();
        try {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                if (key.startsWith(SYMBOL_PREFIX)) {
                    String value = asString(iterator.peekNext().getValue());
                    createDBBySymbol(value, true);
                }
            }
        } finally {
            // Make sure you close the iterator to avoid resource leaks.
            iterator.close();
        }
    }

    @PreDestroy
    public void closeDBs() {
        logger.debug("closeDBs start");
        for (String group : dbMap.keySet()) {
            logger.info("closeDBs start:{}", group);
            DB db = dbMap.get(group);
            try {
                db.close();
                logger.info("closeDBs end:{}", group);
            } catch (Exception e) {
                logger.error("closeDBs failure:{}", group, e);
            }
        }
        logger.debug("closeDBs end");
    }

}
