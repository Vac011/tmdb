package edu.whu.tmdb.query.operations.impl;

import edu.whu.tmdb.query.operations.Exception.ErrorList;
import edu.whu.tmdb.storage.memory.MemManager;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.List;

import edu.whu.tmdb.storage.memory.SystemTable.BiPointerTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.ClassTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.DeputyTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.SwitchingTableItem;
import edu.whu.tmdb.storage.memory.Tuple;
import edu.whu.tmdb.storage.memory.TupleList;
import edu.whu.tmdb.query.operations.CreateDeputyClass;
import edu.whu.tmdb.query.operations.Exception.TMDBException;
import edu.whu.tmdb.query.operations.utils.MemConnect;
import edu.whu.tmdb.query.operations.utils.SelectResult;


public class CreateDeputyClassImpl implements CreateDeputyClass {
    private final MemConnect memConnect;

    public CreateDeputyClassImpl() { this.memConnect = MemConnect.getInstance(MemManager.getInstance()); }

    @Override
    public boolean createDeputyClass(Statement stmt) throws TMDBException, IOException {
        return execute((net.sf.jsqlparser.statement.create.deputyclass.CreateDeputyClass) stmt);
    }

    public boolean execute(net.sf.jsqlparser.statement.create.deputyclass.CreateDeputyClass stmt) throws TMDBException, IOException {
        // 1.获取代理类名、代理类型、select元组
        String deputyClassName = stmt.getDeputyClass().toString();  // 代理类名
        if (memConnect.classExist(deputyClassName)) {
            throw new TMDBException(ErrorList.TABLE_ALREADY_EXISTS, deputyClassName);
        }
        int deputyType = getDeputyType(stmt);   // 代理类型
        Select selectStmt = stmt.getSelect();
        SelectResult selectResult = getSelectResult(selectStmt);
        String deputyRules = selectStmt.toString();  // 代理规则
        // // 取出where语句构建代理规则
        // SelectBody selectBody = selectStmt.getSelectBody();
        // List<SelectBody> plainSelectList = new ArrayList<>();
        // List<String> deputyRules = new ArrayList<>();
        // // 如果是union代理类
        // if (deputyType == 2) {
        //     SetOperationList setOperationList = (SetOperationList) selectBody;
        //     for(SelectBody selectbody : setOperationList.getSelects()) {
        //         plainSelectList.add(selectbody);
        //     }
        // }
        // else {
        //     plainSelectList.add(selectBody);
        // }
        // for (SelectBody selectbody : plainSelectList)
        // {
        //     PlainSelect plainSelect = (PlainSelect) selectbody;
        //     Expression where = plainSelect.getWhere();
        //     if (where != null) {
        //         deputyRules.add(where.toString());
        //     }
        // }
        // 2.执行代理类创建
        return createDeputyClassStreamLine(selectResult, deputyType, new String[]{deputyRules}, deputyClassName);
    }

    public boolean createDeputyClassStreamLine(SelectResult selectResult, int deputyType, String[] deputyRules, String deputyClassName) throws TMDBException, IOException {
        int deputyId = createDeputyClass(deputyClassName, selectResult, deputyType);
        createDeputyTableItem(selectResult.getClassName(), deputyRules, deputyId);
        try {
            createBiPointerTableItem(selectResult, deputyId);
        } catch (TMDBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSQLParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 创建代理类的实现，包含代理类classTableItem的创建和switchingTableItem的创建
     * @param deputyClassName 代理类名称
     * @param selectResult 代理类包含的元组列表
     * @param deputyRule 代理规则
     * @return 新建代理类ID
     */
    private int createDeputyClass(String deputyClassName, SelectResult selectResult, int deputyRule) throws TMDBException {
        // 获取源类的各种信息
        // 字段(属性)对应的类名
        String[] classname = selectResult.getClassName();
        // 字段(属性)名，as后的属性名
        String[] attrname = selectResult.getAttrname();
        // 字段(属性)的select别名，实际为原属性名
        String[] alias = selectResult.getAlias();
        // 字段(属性)的源id，而代理类的属性id是从0开始在selectresult的attr按顺序递增
        int[] attrid = selectResult.getAttrid();
        // 字段(属性)的类型
        String[] type = selectResult.getType();
        // 分配新classid
        MemConnect memConnect = MemConnect.getInstance(MemManager.getInstance());
        MemConnect.getClassTable().maxid++;
        int deputyclassid = MemConnect.getClassTable().maxid;
        // 拓展系数，用于union这类一个代理属性对应多个源的代理类
        int factor = classname.length/attrname.length;
        // 遍历属性条目
        for(int i=0; i<attrname.length; i++){
            // 创建classtable item
            ClassTableItem classtableitem = new ClassTableItem(deputyClassName, deputyclassid, attrname.length, i, attrname[i], type[i], "deputy", "");
            MemConnect.getClassTableList().add(classtableitem);
            // 创建switchingtable item
            for(int j=0; j<factor; j++){
                int origincalssid = memConnect.getClassId(classname[factor*i+j]);
                // switchingtableitem中的代理规则存放的应是a+b as c这类的表达式
                SelectImpl selectExecutor = new SelectImpl();
                ArrayList<String> tablecolumn = new ArrayList<>();
                selectExecutor.attributeParser(alias[factor*i+j], tablecolumn);
                SwitchingTableItem switchingtableitem = new SwitchingTableItem(origincalssid, attrid[factor*i+j], tablecolumn.get(1), deputyclassid, i, attrname[i], alias[factor*i+j]);
                MemConnect.getSwitchingTableList().add(switchingtableitem);
            }
        }
        return deputyclassid;
    }

    /**
     * 新建deputyTableItem
     * @param classNames 源类类名列表
     * @param deputyRule 代理规则
     * @param deputyId 代理类id
     */
    public void createDeputyTableItem(String[] classNames, String[] deputyRules, int deputyId) throws TMDBException {
        MemConnect memConnect = MemConnect.getInstance(MemManager.getInstance());
        HashSet<Integer> originids = new HashSet<>();
        // 需要为每一个(不同的)源class建立一个DeputyTableItem
        for(String classname : classNames){
            int originid = memConnect.getClassId(classname);
            if(originids.contains(originid)){
                continue;
            }
            else{
                originids.add(originid);
                // deputytableitem中的deputyrule存放的应是where后面的表达式
                DeputyTableItem deputyrTableItem = new DeputyTableItem(originid, deputyId, deputyRules);
                MemConnect.getDeputyTableList().add(deputyrTableItem);
            }
        }
    }

    /**
     * 插入元组，并新建BiPointerTableItem
     * @param selectResult 插入的元组列表
     * @param deputyId 新建代理类id
     * @throws JSQLParserException 
     */
    private void createBiPointerTableItem(SelectResult selectResult, int deputyId) throws TMDBException, IOException, JSQLParserException {
        InsertImpl insert = new InsertImpl();
        List<String> columns = new ArrayList<>(Arrays.asList(selectResult.getAttrname()));
        for (Tuple tuple: selectResult.getTpl().tuplelist) {
            // 使用insert.execute()插入对象
            int oriId = tuple.classId;
            int oritupleid = tuple.tupleId;
            int deputytupleid = insert.execute(deputyId, columns, tuple);

            // 可调用getOriginClass(selectResult);
            // HashSet<Integer> originClass = getOriginClass(selectResult);

            // 使用MemConnect.getBiPointerTableList().add()插入BiPointerTable
            BiPointerTableItem biPointerTableItem = new BiPointerTableItem(oriId, oritupleid, deputyId, deputytupleid);
            MemConnect.getBiPointerTableList().add(biPointerTableItem);
        }
    }

    /**
     * 给定创建代理类语句，返回代理规则
     * @param stmt 创建代理类语句
     * @return 代理规则
     */
    private int getDeputyType(net.sf.jsqlparser.statement.create.deputyclass.CreateDeputyClass stmt) {
        switch (stmt.getType().toLowerCase(Locale.ROOT)) {
            case "selectdeputy":    return 0;
            case "joindeputy":      return 1;
            case "uniondeputy":     return 2;
            case "groupbydeputy":   return 3;
        }
        return -1;
    }

    /**
     * 给定查询语句，返回select查询执行结果（创建deputyclass后面的select语句中的selectResult）
     * @param selectStmt select查询语句
     * @return 查询执行结果（包含所有满足条件元组）
     */
    private SelectResult getSelectResult(Select selectStmt) throws TMDBException, IOException {
        SelectImpl selectExecutor = new SelectImpl();
        return selectExecutor.select(selectStmt);
    }

    private HashSet<Integer> getOriginClass(SelectResult selectResult) {
        ArrayList<String> collect = Arrays.stream(selectResult.getClassName()).collect(Collectors.toCollection(ArrayList::new));
        HashSet<String> collect1 = Arrays.stream(selectResult.getClassName()).collect(Collectors.toCollection(HashSet::new));
        HashSet<Integer> res = new HashSet<>();
        for (String s : collect1) {
            res.add(collect.indexOf(s));
        }
        return res;
    }
}
