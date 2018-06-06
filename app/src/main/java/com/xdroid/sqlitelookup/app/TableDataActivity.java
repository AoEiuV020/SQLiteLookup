package com.xdroid.sqlitelookup.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xdroid.sqlitelookup.R;
import com.xdroid.sqlitelookup.adapter.SimpleTableAdapter;
import com.xdroid.sqlitelookup.dialog.SelectorDialog;
import com.xdroid.sqlitelookup.model.ColumnInfo;
import com.xdroid.sqlitelookup.model.SqliteMaster;
import com.xdroid.sqlitelookup.utils.AppUtils;
import com.xdroid.sqlitelookup.utils.SqlUtils;
import com.xdroid.tablefixheaders.TableFixHeaders;
import com.xdroid.utils.sqlite.DaoFactory;
import com.xdroid.utils.sqlite.DbSqlite;
import com.xdroid.utils.sqlite.IBaseDao;
import com.xdroid.utils.sqlite.PagingList;
import com.xdroid.utils.sqlite.ResultSet;
import com.xdroid.utils.sqlite.Table.Column;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class TableDataActivity extends BaseActivity implements View.OnClickListener , SelectorDialog.OnItemSelectedListener {

	public static final String EXTRA_TABLE_NAME = "table-name";
	public static final String EXTRA_DB_PATH = "db-path";
	private static final int MAX_TEXT_LEN = 30;
	
	private ImageView mIvQuery;
	private View mLayoutQuery;
	private EditText mEtSql;
	private TextView mTvError;
	private Button mBtnQuery;
	private TableFixHeaders mTables;
	private TableDataAdapter mTableDataAdapter;
	private List<ResultSet> mTableData;
	private String mTableName;
	private String mDbPath;
	
	private List<ColumnInfo> mColumnInfoList;
	private int mPage = 1;
	private String mOrderBy;
	
	private ResultSet mSelectedRecord;
	private SelectorDialog mDlgSelector;
	private static final String[] SELECT_ITEMS = {"CHECK RECORD DETAIL"};
	
	private ProgressDialog mDlgLoading;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_table_data);
		mIvQuery = findView(R.id.iv_right);
		mIvQuery.setVisibility(View.VISIBLE);
		mIvQuery.setImageResource(R.drawable.ic_edit_sql);
		mIvQuery.setOnClickListener(this);
		Intent extraIntent = getIntent();
		mTableName = extraIntent.getStringExtra(EXTRA_TABLE_NAME);
		mDbPath = extraIntent.getStringExtra(EXTRA_DB_PATH);
		mTables = findView(R.id.table);
		mLayoutQuery = findView(R.id.layout_sql_query);
		mEtSql = findView(R.id.et_raw_sql);
		mBtnQuery = findView(R.id.btn_execute_sql);
		mBtnQuery.setOnClickListener(this);
		mTvError = findView(R.id.tv_sql_error);
		enableBack();
		setMainTitle(String.format("Data In %s", mTableName));
		mDlgSelector = new SelectorDialog(this);
		mDlgSelector.setSelectItems(SELECT_ITEMS, this);
		mDlgLoading = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
		mDlgLoading.setMessage(getString(R.string.loading));
		mDlgLoading.setCancelable(false);
		mDlgLoading.setCanceledOnTouchOutside(false);
		listTableData();
	}
	
	@Override
	public void onSelected(int position) {
		if(position == 0){
			Intent detailIntent = new Intent(this,RecordDetailActivity.class);
			detailIntent.putExtra(RecordDetailActivity.EXTRA_RECORD, mSelectedRecord);
			startActivity(detailIntent);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.iv_right:
			onQueryClick();
			break;
		case R.id.btn_execute_sql:
			performExecuteSql();
			break;
		}
	}
	
	private void onQueryClick(){
		int visible = mLayoutQuery.getVisibility();
		if(visible == View.GONE){
			mEtSql.setText(String.format("SELECT * FROM %s ", mTableName));
			mLayoutQuery.setVisibility(View.VISIBLE);
		}else{
			mTvError.setVisibility(View.GONE);
			mLayoutQuery.setVisibility(View.GONE);
		}
	}
	
	private void performExecuteSql(){
		String querySql = mEtSql.getText().toString().trim();
		if(!querySql.isEmpty()){
			hideInputMethod();
			
			if(querySql.endsWith(";")){
				querySql = querySql.substring(0, querySql.length() - 1);
			}
			
			new QuerySqlTask().execute(querySql);
		}
	}
	
	private void listTableData(){
		new GetTableDataTask().execute();
	}
	
	private void listMoreTableData(){
		++mPage;
		new GetTableDataTask().execute();
	}
	
	private void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEtSql.getWindowToken(), 0);
	}
	
	class QuerySqlTask extends AsyncTask<String, SQLException, List<ResultSet>>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDlgLoading.show();
		}
		
		@Override
		protected List<ResultSet> doInBackground(String... params) {
			String querySql = params[0];
			SQLiteDatabase db = SQLiteDatabase.openDatabase(mDbPath, null,  SQLiteDatabase.OPEN_READONLY);
			DbSqlite dbSqlite = new DbSqlite(null, db);
			List<ResultSet> resultSetList = null;
			try{
				resultSetList = dbSqlite.execQuerySQL(querySql);
				if(resultSetList != null && !resultSetList.isEmpty()){
					ResultSet resultSet = resultSetList.get(0);
					int setSize = resultSet.getSize();
					List<ColumnInfo> columnInfoList = new ArrayList<ColumnInfo>();
					ColumnInfo columnInfo;
					for(int colIndex = 0; colIndex < setSize ; ++colIndex){
						columnInfo = new ColumnInfo();
						columnInfo.setName(resultSet.getColumnName(colIndex));
						columnInfoList.add(columnInfo);
					}
					mColumnInfoList = columnInfoList;
				}
			}catch(SQLException ex){
				ex.printStackTrace();
				publishProgress(ex);
			}finally{
				dbSqlite.closeDB();
			}
			return resultSetList;
		}
		
		@Override
		protected void onProgressUpdate(SQLException... values) {
			super.onProgressUpdate(values);
			SQLException ex = values[0];
			mTvError.setText(ex.getMessage());
			mTvError.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(List<ResultSet> result) {
			mDlgLoading.hide();
			if(result != null){
				mTableData.clear();
				mTableData.addAll(result);
				mTableDataAdapter.notifyDataSetChanged();
				mTvError.setVisibility(View.GONE);
			}
		}
	}
	
	class GetTableDataTask extends AsyncTask<Void, Void, List<ResultSet>>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDlgLoading.show();
		}
		
		@Override
		protected List<ResultSet> doInBackground(Void... params) {
			SQLiteDatabase db = SQLiteDatabase.openDatabase(mDbPath, null,  SQLiteDatabase.OPEN_READONLY);
			DbSqlite dbSqlite = new DbSqlite(null, db);
			if(mColumnInfoList == null){
				IBaseDao<SqliteMaster> masterDao = DaoFactory.createGenericDao(dbSqlite, SqliteMaster.class);
				SqliteMaster table = masterDao.queryFirstRecord("type=? and name=?", "table",mTableName);
				List<ColumnInfo> columnInfoList = new ArrayList<ColumnInfo>();
				SqlUtils.resolveCreateSql(table.sql, columnInfoList);
				mOrderBy = getOrderColumn(columnInfoList);
				mColumnInfoList = columnInfoList;
			}
			String orderBy = mOrderBy;
			PagingList<ResultSet> result = dbSqlite.pagingQuery(mTableName, null, null, null, null, null, orderBy, mPage, 50);
			dbSqlite.closeDB();
			return result;
		}
		
		@Override
		protected void onPostExecute(List<ResultSet> result) {
			super.onPostExecute(result);
			mDlgLoading.hide();
			
			if(mTableDataAdapter == null){
				if(result != null){
					mTableData = new ArrayList<ResultSet>();
					mTableData.addAll(result);
				}
				mTableDataAdapter = new TableDataAdapter(TableDataActivity.this, mColumnInfoList, mTableData);
				mTables.setAdapter(mTableDataAdapter);
				return;
			}
			
			if(result != null){
				mTableData.addAll(result);
				mTableDataAdapter.notifyDataSetChanged();
			}
		}
		
		private String getOrderColumn(List<ColumnInfo> columnInfoList){
			for(ColumnInfo colInfo : columnInfoList){
				if(colInfo.isPrimaryKey())
					return colInfo.getName();
			}
			return columnInfoList.get(0).getName();
		}
	}
	
	class TableDataAdapter extends SimpleTableAdapter<ColumnInfo, ResultSet> {

		public TableDataAdapter(Context context, List<ColumnInfo> headData,
				List<ResultSet> cellData) {
			super(context, headData, cellData);
		}

		@Override
		public int getWidth(int column) {
			String type = mColumnInfoList.get(column+1).getType();
			String name = mColumnInfoList.get(column + 1).getName();
			Context context = getContext();
			if(type != null){
				if(type.equals(Column.TYPE_STRING)){
					return AppUtils.dipToPx(context, 100);
				} else if (type.equals(Column.TYPE_INTEGER)
						&& (name.contains("date") || name.contains("time"))) {
					return AppUtils.dipToPx(context, 150);
				}else if(type.equals(Column.TYPE_BOOLEAN)){
					return AppUtils.dipToPx(context, 50);
				}else if(type.equals(Column.TYPE_BLOB)){
					return AppUtils.dipToPx(context, 100);
				}else if(type.equals(Column.TYPE_TIMESTAMP)){
					return AppUtils.dipToPx(context, 100);
				}else{
					return AppUtils.dipToPx(context, 100);
				}
			}else{
				return AppUtils.dipToPx(context, 100);
			}
		}

		@Override
		public void bindHeaderText(TextView tvHeader, int column,
				ColumnInfo hRecord) {
			tvHeader.setText(hRecord.getName());
		}

		private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		@Override
		public void bindCellText(TextView tvCell, int row, int column,
								 final ResultSet cRecord) {
			Object cellValue = cRecord.getValue(column+1);
			if(cellValue != null){
				// 列展示在这里，
				String cellStr;
				cellStr = cellValue.toString();
				if(cellStr.length() > MAX_TEXT_LEN){
					cellStr = cellStr.substring(0, MAX_TEXT_LEN - 3);
					cellStr += "...";
				}
				String columnName = cRecord.getColumnName(column + 1);
				if (columnName.contains("date")
						|| columnName.contains("time")) {
					try {
						long time = (long) cellValue;
						cellStr = sdf.format(new Date(time));
					} catch (Exception ignored) {
					}
				}
				tvCell.setText(cellStr);
			}else{
				tvCell.setText("(null)");
			}
			
			tvCell.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					mSelectedRecord = cRecord;
					mDlgSelector.show();
					return true;
				}
			});
		}
	}
}
