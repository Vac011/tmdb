package edu.whu.tmdb;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.whu.tmdb.query.Transaction;
import edu.whu.tmdb.query.operations.utils.SelectResult;
import edu.whu.tmdb.util.DbOperation;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class Main {
    public static String function1(String sqlCommand){
        // 调试用
        System.out.print("tmdb> ");
        if ("resetdb".equalsIgnoreCase(sqlCommand)) {
            return DbOperation.resetDB();
        } else if ("show BiPointerTable".equalsIgnoreCase(sqlCommand)) {
            return DbOperation.showBiPointerTable();
        } else if ("show ClassTable".equalsIgnoreCase(sqlCommand)) {
           return DbOperation.showClassTable();
        } else if ("show DeputyTable".equalsIgnoreCase(sqlCommand)) {
            return DbOperation.showDeputyTable();
        } else if ("show SwitchingTable".equalsIgnoreCase(sqlCommand)) {
            return DbOperation.showSwitchingTable();
        } else if (!sqlCommand.isEmpty()) {
            try {
                SelectResult result = execute(sqlCommand);
                if (result != null) {
                    return DbOperation.printResultToString(result);
                } else {
                    return "OK!";
                }
            } catch (JSQLParserException e) {
                return "Syntax error!";
            }
        }
        return "Wrong Input!";
    }

    public static void insertIntoTrajTable(){
        Transaction transaction = Transaction.getInstance();
        transaction.insertIntoTrajTable();
        transaction.SaveAll();
    }

    public static void testEngine() throws IOException {
        Transaction transaction = Transaction.getInstance();
        transaction.testEngine();
    }

    public static void testMapMatching() {
        Transaction transaction = Transaction.getInstance();
        transaction.testMapMatching();
    }

    public static SelectResult execute(String s) throws JSQLParserException {
        Transaction transaction = Transaction.getInstance();    // 创建一个事务实例
        SelectResult selectResult = null;
        try {
            // 使用JSqlparser进行sql语句解析，会根据sql类型生成对应的语法树
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
            Statement stmt = CCJSqlParserUtil.parse(byteArrayInputStream);
            selectResult = transaction.query("", -1, stmt);
            if (!stmt.getClass().getSimpleName().toLowerCase().equals("select")) {
                transaction.SaveAll();
            }
        } catch (JSQLParserException e) {
            // 返回一个特定的SelectResult，表示语法错误
            throw e;
        }
        return selectResult;
    }

}