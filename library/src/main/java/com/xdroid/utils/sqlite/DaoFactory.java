package com.xdroid.utils.sqlite;


/**
 * use this factory to create your sqlite data access object
 */
public class DaoFactory {

    /**
     * call this method to new a GenericDao
     *
     * @param db
     * @param modelClazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> IBaseDao<T> createGenericDao(DbSqlite db, Class<?> modelClazz) {
        return new GenericDao<T>(db, modelClazz);
    }

}
