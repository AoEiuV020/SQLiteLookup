package com.xdroid.sqlitelookup.utils;

import com.xdroid.sqlitelookup.model.ColumnInfo;

import java.util.List;

public class SqlUtils {

	public static void resolveCreateSql(String createSql,List<ColumnInfo> columnInfoList){
		createSql = preProcessSql(createSql);

        // 针对有索引的情况，建表语句外面也有括号，
        int last = 0;
        int leftCount = 0;
        char[] chs = createSql.toCharArray();
        for (int i = 0; i < chs.length; i++) {
            char ch = chs[i];
            if (ch == '(') {
                leftCount++;
            } else if (ch == ')') {
                last = i;
                leftCount--;
                if (leftCount == 0) {
                    break;
                }
            }
        }
        String subSql = createSql.substring(createSql.indexOf('(') + 1, last);
		String[] columnInfos = subSql.split(",");

		ColumnInfo columnInfo;
		String columnInfoStr;
		String upperColumnInfoStr;
		int firstBlankIndex,secBlankIndex,defaultBeginIndex,defaultEndIndex;
		String name,type,defaultValue;
		for(int i = 0; i < columnInfos.length ; ++i){
			columnInfo = new ColumnInfo();
			// 列信息处理在这里，
			columnInfoStr = columnInfos[i].trim();
            System.out.println("columnInfoStr = " + columnInfoStr);
            if (columnInfoStr.contains("(") || columnInfoStr.contains(")")) {
                // 针对有些主键外键单独写成一个字段的情况，
                // 有括号直接跳过，
                continue;
            }
			firstBlankIndex = columnInfoStr.indexOf(' ');
			secBlankIndex = columnInfoStr.indexOf(' ', firstBlankIndex+1);
			secBlankIndex = secBlankIndex == -1 ? columnInfoStr.length() : secBlankIndex;
			name = columnInfoStr.substring(0, firstBlankIndex);
			type = columnInfoStr.substring(firstBlankIndex+1, secBlankIndex);
			columnInfo.setName(name);
			columnInfo.setType(type);
			upperColumnInfoStr = columnInfoStr.toUpperCase();
			defaultBeginIndex = upperColumnInfoStr.indexOf("DEFAULT");
			if(defaultBeginIndex != -1){
				defaultBeginIndex = defaultBeginIndex + 8;
				int semiIndex = columnInfoStr.indexOf("'",defaultBeginIndex);
				if(semiIndex != -1){
					defaultBeginIndex = semiIndex + 1;
					defaultEndIndex = columnInfoStr.lastIndexOf("'");
				}else{
					semiIndex = columnInfoStr.indexOf("\"", defaultBeginIndex);
					if(semiIndex != -1){
						defaultBeginIndex = semiIndex + 1;
						defaultEndIndex = columnInfoStr.lastIndexOf("\"");
					}else{
						defaultEndIndex = columnInfoStr.indexOf(' ', defaultBeginIndex);
						defaultEndIndex = defaultEndIndex == -1 ?  columnInfoStr.length() : defaultEndIndex;
					}
				}
				defaultValue = columnInfoStr.substring(defaultBeginIndex, defaultEndIndex);
				columnInfo.setDefaultValue(defaultValue);
			}


            if(upperColumnInfoStr.contains("PRIMARY KEY")){
				columnInfo.setPrimaryKey(true);
			}

            if(upperColumnInfoStr.contains("NOT NULL")){
				columnInfo.setNull(false);
			}

            if(upperColumnInfoStr.contains("UNIQUE")){
				columnInfo.setUnique(true);
			}
			columnInfoList.add(columnInfo);
		}
	}

    private static String preProcessSql(String sql){
		StringBuilder resultSql = new StringBuilder();
		char[] chs = sql.trim().toCharArray();

        boolean isText = false;
		boolean isLastBlank = false;
		for(char ch : chs){

            if(ch != ' '|| !isLastBlank || isText){
				resultSql.append(ch);
			}

            if(ch == '\"' || ch == '\''){
				isText = !isText;
			}else if(ch == ' '){
				isLastBlank = true;
			}else{
				isLastBlank = false;
			}
		}

        return resultSql.toString();
	}

}
