package edu.whu.tmdb.util;

import android.util.Log;

import edu.whu.tmdb.App;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.whu.tmdb.query.operations.utils.MemConnect;
import edu.whu.tmdb.query.operations.utils.SelectResult;
import edu.whu.tmdb.storage.memory.SystemTable.BiPointerTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.ClassTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.DeputyTableItem;
import edu.whu.tmdb.storage.memory.SystemTable.SwitchingTableItem;
import edu.whu.tmdb.storage.memory.Tuple;
import edu.whu.tmdb.storage.memory.TupleList;

import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbOperation {
    /**
     * 给定元组查询结果，输出查询表格
     * @param result 查询语句的查询结果
     */
    /**
     * 给定元组查询结果，输出查询表格
     * @param result 查询语句的查询结果
     */
    public static void printResult(SelectResult result) {
        // 输出表头信息
        StringBuilder tableHeader = new StringBuilder("|");
        for (int i = 0; i < result.getAttrname().length; i++) {
            // 指定输出字段最小宽度为25个字符，空格填充
            tableHeader.append(String.format("%-25s", result.getClassName()[i] + "." + result.getAttrname()[i])).append("|");
        }
        System.out.println(tableHeader);

        // 输出元组信息
        for (Tuple tuple : result.getTpl().tuplelist) {
            StringBuilder data = new StringBuilder("|");
            for (int i = 0; i < tuple.tuple.length; i++) {
                data.append(String.format("%-25s", tuple.tuple[i].toString())).append("|");
            }
            System.out.println(data);
        }
    }

    /**
     * 将查询结果转换为字符串
     * @param result 查询语句的查询结果
     * @return 查询结果的字符串表示
     */
    public static String printResultToString(SelectResult result) {
        StringBuilder resultString = new StringBuilder();

        // 输出表头信息
        StringBuilder tableHeader = new StringBuilder("|");
        for (int i = 0; i < result.getAttrname().length; i++) {
            // 指定输出字段最小宽度为25个字符，空格填充
            String classname = "";
            // 指定输出字段最小宽度为25个字符，空格填充
            tableHeader.append(String.format("%-10s", /*result.getClassName()[i] + "." +*/ result.getAttrname()[i])).append("|");
        }
        resultString.append(tableHeader).append("\n");

        // 输出分隔线
        StringBuilder separator = new StringBuilder("|");
        for (int i = 0; i < result.getAttrname().length; i++) {
            separator.append("----------|");
        }
        resultString.append(separator).append("\n");

        // 输出元组信息
        for (Tuple tuple : result.getTpl().tuplelist) {
            StringBuilder data = new StringBuilder("|");
            for (int i = 0; i < tuple.tuple.length; i++) {
                data.append(String.format("%-10s", tuple.tuple[i].toString())).append("|");
            }
            resultString.append(data).append("\n");
        }

        return resultString.toString();
    }

    /**
     * 删除数据库所有数据文件，即重置数据库
     */
    public static String resetDB() {
        // 仓库路径
       // String repositoryPath = "D:\\desktop\\DB\\tmdb";
        String repositoryPath = App.context.getCacheDir().getAbsolutePath();
        Log.d("resetDB", "Repository path: " + repositoryPath);
        // 子目录路径
        String sysPath = repositoryPath + File.separator + "data" + File.separator + "sys";
        String logPath = repositoryPath + File.separator + "data" + File.separator + "log";
        String levelPath = repositoryPath + File.separator + "data" + File.separator + "level";

        List<String> filePath = new ArrayList<>();
        filePath.add(sysPath);
        filePath.add(logPath);
        filePath.add(levelPath);

        // 遍历删除文件
        for (String path : filePath) {
            File directory = new File(path);

            // 检查目录是否存在
            if (!directory.exists()) {
                Log.d("resetDB", "目录不存在：" + path);
            }
            Log.d("resetDB", "目录存在：" + path);
            // 获取目录中的所有文件
            File[] files = directory.listFiles();
            if (files == null) {
                Log.d("resetDB", "目录为空！");
                continue;
            }
            Log.d("resetDB", "目录不为空！" + Arrays.toString(files));
            for (File file : files) {
                // 删除文件
//                if (file.delete()) {
//                    Log.d("resetDB", "已删除文件：" + file.getAbsolutePath());
//                } else {
//                    Log.d("resetDB", "无法删除文件：" + file.getAbsolutePath());
//                }
                // 调试用
                Log.d("resetDB", "1");
                 Path filepath = file.toPath();
                Log.d("resetDB", "2");
                 try {
                     Log.d("resetDB","trying to delete file: " + filepath);
                     Files.delete(filepath);
                 } catch (IOException e) {
                     Log.d("resetDB","Failed to delete file: " + e.getMessage());
                 }
            }
        }
        return "删除成功！";
    }

    public static String showBiPointerTable() {
        edu.whu.tmdb.query.Transaction transaction =edu.whu.tmdb.query.Transaction.getInstance();
        // 通过printresult输出
        List<String> head = Arrays.asList("classid", "objectid", "deputyid", "deputyobjectid");
        SelectResult result = new SelectResult(head.size());
        for(int i = 0; i < head.size();i++){
            result.getClassName()[i] = "BPTable";
            result.getAttrname()[i] = head.get(i);
        }
        TupleList tpl = new TupleList();
        for (BiPointerTableItem item : MemConnect.getBiPointerTableList()){
            Object[] tupleValues = {item.classid, item.objectid, item.deputyid, item.deputyobjectid};
            Tuple tuple = new Tuple(tupleValues);
            tpl.addTuple(tuple);
        }
        result.setTpl(tpl);
        return printResultToString(result);
        // 直接print输出
        // for (BiPointerTableItem item : MemConnect.getBiPointerTableList()){
        //     System.out.printf("|%d\t\t\t|%d\t\t\t|%d\t\t\t|%d\n", item.classid, item.objectid, item.deputyid, item.deputyobjectid);
        // }
    }

    public static String showClassTable() {
        edu.whu.tmdb.query.Transaction transaction =edu.whu.tmdb.query.Transaction.getInstance();
        List<String> head = Arrays.asList("classname", "classid", "attrname", "attrid", "attrtype");
        SelectResult result = new SelectResult(head.size());
        for(int i = 0; i < head.size();i++){
            result.getClassName()[i] = "ClassTable";
            result.getAttrname()[i] = head.get(i);
        }
        TupleList tpl = new TupleList();
        for (ClassTableItem item : MemConnect.getClassTableList()){
            Object[] tupleValues = {item.classname, item.classid, item.attrname, item.attrid, item.attrtype};
            Tuple tuple = new Tuple(tupleValues);
            tpl.addTuple(tuple);
        }
        result.setTpl(tpl);
        return printResultToString(result);
        // for(ClassTableItem item : MemConnect.getClassTableList()){
        //     System.out.printf("|%s\t\t\t|%d\t\t\t|%s\t\t\t|%d\t\t\t|%s\t\t\t|\n", item.classname, item.classid, item.attrname, item.attrid, item.attrtype);
        // }
    }

    public static String showDeputyTable() {
        edu.whu.tmdb.query.Transaction transaction =edu.whu.tmdb.query.Transaction.getInstance();
        List<String> head = Arrays.asList("originid", "deputyid");
        SelectResult result = new SelectResult(head.size());
        for(int i = 0; i < head.size();i++){
            result.getClassName()[i] = "DeputyTable";
            result.getAttrname()[i] = head.get(i);
        }
        TupleList tpl = new TupleList();
        for (DeputyTableItem item : MemConnect.getDeputyTableList()){
            Object[] tupleValues = {item.originid, item.deputyid};
            Tuple tuple = new Tuple(tupleValues);
            tpl.addTuple(tuple);
        }
        result.setTpl(tpl);
        return printResultToString(result);
        // for (DeputyTableItem item : MemConnect.getDeputyTableList()){
        //     System.out.printf("originclassID: %d\tdeputyclassID: %d\n", item.originid, item.deputyid);
        // }
    }

    public static String showSwitchingTable() {
        edu.whu.tmdb.query.Transaction transaction =edu.whu.tmdb.query.Transaction.getInstance();
        List<String> head = Arrays.asList("oriId", "oriAttrid", "oriAttr", "deputyId", "deputyAttrId", "deputyAttr", "deputyRule");
        SelectResult result = new SelectResult(head.size());
        for(int i = 0; i < head.size();i++){
            result.getClassName()[i] = "SwitchTable";
            result.getAttrname()[i] = head.get(i);
        }
        TupleList tpl = new TupleList();
        for (SwitchingTableItem item : MemConnect.getSwitchingTableList()){
            // // 加这个是因为在修改代码或某些情况下switchingtableitem的oriattr和deputyattr不知道为什么为变成null
            // String oriAttr = (item.oriAttr == null)? "null":item.oriAttr;
            // String deputyAttr = (item.deputyAttr == null)? "null":item.deputyAttr;

            Object[] tupleValues = {item.oriId, item.oriAttrid, item.oriAttr, item.deputyId, item.deputyAttrId, item.deputyAttr, item.rule};
            Tuple tuple = new Tuple(tupleValues);
            tpl.addTuple(tuple);
        }
        result.setTpl(tpl);
        return printResultToString(result);
        // for (SwitchingTableItem item : MemConnect.getSwitchingTableList()){
        //     System.out.printf("%d\t%s\t%d\t%d\t%s\n", item.oriAttrid, item.oriAttr, item.deputyId, item.deputyAttrId, item.deputyAttr);
        // }
    }
}
