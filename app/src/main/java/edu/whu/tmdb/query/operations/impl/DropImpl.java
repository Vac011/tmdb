package edu.whu.tmdb.query.operations.impl;

import edu.whu.tmdb.storage.memory.MemManager;
import net.sf.jsqlparser.statement.Statement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

//import org.checkerframework.checker.units.qual.C;
//import org.checkerframework.checker.units.qual.s;

import edu.whu.tmdb.storage.memory.SystemTable.BiPointerTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.ClassTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.DeputyTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.ObjectTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.SwitchingTableItem;
import edu.whu.tmdb.query.operations.Exception.TMDBException;
import edu.whu.tmdb.query.operations.Drop;
import edu.whu.tmdb.query.operations.utils.MemConnect;

public class DropImpl implements Drop {

    private MemConnect memConnect;

    public DropImpl() {
        this.memConnect = MemConnect.getInstance(MemManager.getInstance());
    }

    @Override
    public boolean drop(Statement statement) throws TMDBException {
        return execute((net.sf.jsqlparser.statement.drop.Drop) statement);
    }

    public boolean execute(net.sf.jsqlparser.statement.drop.Drop drop) throws TMDBException {
        String tableName = drop.getName().getName();
        int classId = memConnect.getClassId(tableName);
        drop(classId);
        return true;
    }

    public void drop(int classId) {
        ArrayList<Integer> deputyClassIdList = new ArrayList<>();   // 存储该类对应所有代理类id

        dropClassTable(classId);                            // 1.删除ClassTableItem
        dropDeputyClassTable(classId, deputyClassIdList);   // 2.获取代理类id并在表中删除
        dropBiPointerTable(classId);                        // 3.删除 源类/对象<->代理类/对象 的双向关系表
        dropSwitchingTable(classId);                        // 4.删除switchingTable
        dropObjectTable(classId);                           // 5.删除已创建的源类对象

        // 6.递归删除代理类对象
        while(!deputyClassIdList.isEmpty()){
            int deputyClassId = deputyClassIdList.remove(0);
            drop(deputyClassId);
        }
    }

    /**
     * 给定要删除的class id，删除系统表类表(class table)中的表项
     * @param classId 要删除的表对应的id
     */
    private void dropClassTable(int classId) {
        ArrayList<ClassTableItem> classTableList = new ArrayList<>();
        for (ClassTableItem item : MemConnect.getClassTableList()){
            if (item.classid == classId){
                classTableList.add(item);
            }
        }
        for (ClassTableItem item : classTableList){
            MemConnect.getClassTableList().remove(item);
        }
    }

    /**
     * 删除系统表中的deputy table，并获取class id对应源类的代理类id
     * @param classId 源类id
     * @param deputyClassIdList 作为返回值，源类对应的代理类id列表
     */
    private void dropDeputyClassTable(int classId, ArrayList<Integer> deputyClassIdList) {
        ArrayList<DeputyTableItem> deputyTableList = new ArrayList<>();
        for (DeputyTableItem item : MemConnect.getDeputyTableList()){
            // 不仅自身为源类的项要删除，自身为代理类的项也要删除
            if (item.originid == classId || item.deputyid == classId){
                deputyClassIdList.add(item.deputyid);
                deputyTableList.add(item);
            }
        }
        for (DeputyTableItem item : deputyTableList){
            MemConnect.getDeputyTableList().remove(item);
        }
        
    }

    /**
     * 删除系统表中的BiPointerTable
     * @param classId 源类id
     */
    private void dropBiPointerTable(int classId) {
        ArrayList<BiPointerTableItem> biPointerTableList = new ArrayList<>();
        for (BiPointerTableItem item : MemConnect.getBiPointerTableList()){
            if (item.classid == classId || item.deputyid == classId){
                biPointerTableList.add(item);
            }
        }
        for (BiPointerTableItem item : biPointerTableList){
            MemConnect.getBiPointerTableList().remove(item);
        }
    }

    /**
     * 删除系统表中的SwitchingTable
     * @param classId 源类id
     */
    private void dropSwitchingTable(int classId) {
        ArrayList<SwitchingTableItem> switchingTableList = new ArrayList<>();
        for(SwitchingTableItem item : MemConnect.getSwitchingTableList()){
            if (item.oriId == classId || item.deputyId == classId){
                switchingTableList.add(item);
            }
        }
        for (SwitchingTableItem item : switchingTableList){
            MemConnect.getSwitchingTableList().remove(item);
        }
    }

    /**
     * 删除源类具有的所有对象及objecttable中的项
     * @param classId 源类id
     */
    private void dropObjectTable(int classId) {
        MemConnect memConnect = MemConnect.getInstance(MemManager.getInstance());
        ArrayList<ObjectTableItem> objectTableList = new ArrayList<>();
        for(ObjectTableItem item : MemConnect.getObjectTableList()){
            if (item.classid == classId){
                // 删除元组
                memConnect.DeleteTuple(item.tupleid);
                // 获取objecttable表项
                objectTableList.add(item);
            }
        }
        for (ObjectTableItem item : objectTableList){
            MemConnect.getObjectTableList().remove(item);
        }
    }

}
