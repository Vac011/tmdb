package edu.whu.tmdb.query.operations.impl;


import edu.whu.tmdb.storage.memory.MemManager;
import edu.whu.tmdb.storage.memory.SystemTable.BiPointerTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.ObjectTableItem;
import edu.whu.tmdb.storage.memory.Tuple;
import edu.whu.tmdb.storage.memory.TupleList;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import edu.whu.tmdb.query.operations.Exception.TMDBException;
import edu.whu.tmdb.query.operations.Delete;
import edu.whu.tmdb.query.operations.Select;
import edu.whu.tmdb.query.operations.utils.MemConnect;
import edu.whu.tmdb.query.operations.utils.SelectResult;

public class DeleteImpl implements Delete {
    //define a object
    private MemConnect memConnect;

    //to private a global use
    public DeleteImpl() {this.memConnect = MemConnect.getInstance(MemManager.getInstance());}

    @Override
    public void delete(Statement statement) throws JSQLParserException, TMDBException, IOException {
        execute((net.sf.jsqlparser.statement.delete.Delete) statement);
    }

    public void execute(net.sf.jsqlparser.statement.delete.Delete deleteStmt) throws JSQLParserException, TMDBException, IOException {
        // 1.获取符合where条件的所有元组
        Table table = deleteStmt.getTable();        // 获取需要删除的表名
        Expression where = deleteStmt.getWhere();   // 获取delete中的where表达式
        String sql = "select * from " + table;;
        if (where != null) {
            sql += " where " + String.valueOf(where) + ";";
        }
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sql.getBytes());
        net.sf.jsqlparser.statement.select.Select parse = (net.sf.jsqlparser.statement.select.Select) CCJSqlParserUtil.parse(byteArrayInputStream);
        Select select = new SelectImpl();
        SelectResult selectResult = select.select(parse);

        // 2.执行delete
        delete(selectResult.getTpl());
    }

    public void delete(TupleList tupleList) {
        // 1.删除源类tuple和object table
        for(Tuple tuple : tupleList.tuplelist){
            memConnect.DeleteTuple(tuple.tupleId);
            ObjectTableItem object_item = new ObjectTableItem(tuple.classId,tuple.tupleId);
            // 使用MemConnect.getObjectTableList().remove()除对象表
            MemConnect.getObjectTableList().remove(object_item);
        }

        // 2.删除源类biPointerTable
        // 获取对应的biPointerItem
        ArrayList<BiPointerTableItem> bilist = new ArrayList<>();
        ArrayList<Integer> deputyTupleIdList = new ArrayList<>();
        for(Tuple tuple : tupleList.tuplelist){
            for(BiPointerTableItem biPointerTableItem : MemConnect.getBiPointerTableList()){
                // 这里不仅要删除自身的id对应的objectid项，还要删除对应的deputyobjectid项
                if(tuple.tupleId == biPointerTableItem.objectid || tuple.tupleId == biPointerTableItem.deputyobjectid){
                    // 只有自己对应的objectid项需要递归删除
                    if(tuple.tupleId == biPointerTableItem.objectid){
                        deputyTupleIdList.add(biPointerTableItem.deputyobjectid);
                    }
                    bilist.add(biPointerTableItem);
                }
            }
        }
        // 删除biPointerItem 并构造代理对象Tuple ID
        // 这里不能在上面查询时直接删除，会出现nullPointer的错误
        for(BiPointerTableItem item:bilist){
            // 使用MemConnect.getBiPointerTableList().remove();
            MemConnect.getBiPointerTableList().remove(item);
        }
            
        // 3.根据biPointerTable递归删除代理类相关表
         if (deputyTupleIdList.isEmpty()) { return; }
         TupleList deputyTupleList = new TupleList();
         for (Integer deputyTupleId : deputyTupleIdList) {
             Tuple tuple = memConnect.GetTuple(deputyTupleId);
             deputyTupleList.addTuple(tuple);
         }
         delete(deputyTupleList);
    }

}
