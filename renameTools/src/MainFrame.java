import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.acl.LastOwnerException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static java.awt.datatransfer.DataFlavor.javaFileListFlavor;

public class MainFrame extends JFrame {
    private JPanel jp1 = new JPanel();
    private JLabel jl1;
    private JLabel jl2;
    private JLabel jl3;
    private JLabel jl4;
    private JTextField jt1;
    private JTextField jt2;
    private JButton jb1;
    private JLabel jl5;
    private String numberFormat;
    private int begin;
    static int finished;
    static int total;


    public MainFrame(){
        jl1=new JLabel("将文件夹拖拽到这里");
        jl1.setForeground(Color.RED);
        jl1.setBounds(20, 0, 200, 30);
        jl2=new JLabel("该步骤不可回退，请谨慎操作！");
        jl2.setForeground(Color.RED);
        jl2.setBounds(20, 30, 200, 30);
        jl3=new JLabel("设置命名模板，例如【a_####】");
        jl3.setBounds(20,60,230,30);
        jt1=new JTextField();
        jt1.setBounds(240,60,150,30);
        jt1.setText("####");
        jl4=new JLabel("起始于");
        jl4.setBounds(400,60,50,30);
        jt2=new JTextField();
        jt2.setText("1");
        jt2.setBounds(460,60,40,30);
        jb1=new JButton();
        jb1.setText("应用");
        jb1.setBounds(510,60,70,30);;
        jl5=new JLabel();
        jl5.setBounds(20,530,200,30);
        jl5.setText("已完成0");
        jp1.setLayout(null);
        jp1.add(jl1);
        jp1.add(jl2);
        jp1.add(jl3);
        jp1.add(jt1);
        jp1.add(jl4);
        jp1.add(jt2);
        jp1.add(jb1);
        jp1.add(jl5);
        getContentPane().add(jp1,BorderLayout.CENTER);
        setSize(600,600);
        setTitle("文件重命名");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(400,200);

        numberFormat="####";
        begin=1;
        jb1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jt1.getText().equals("") || jt2.getText().equals("") ||(!Pattern.compile("[0-9]*").matcher(jt2.getText().trim()).matches())){
                    JOptionPane.showMessageDialog(null,"参数有误");
                }else{
                    JOptionPane.showMessageDialog(null,"设置成功");
                    numberFormat=jt1.getText();
                    begin=Integer.valueOf(jt2.getText());
                }

            }
        });
        new DropTarget(jp1, DnDConstants.ACTION_MOVE, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {

            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {

            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {

            }

            @Override
            public void dragExit(DropTargetEvent dte) {

            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable tr = dtde.getTransferable();

                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                        List list = (List) (dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
                        Iterator iterator = list.iterator();
                        while (iterator.hasNext()) {
                            File f = (File) iterator.next();
                            System.out.println("你拖入的文件是："+f.getAbsolutePath());
                            finished = 0;
                            total=0;
                            rename(f.getAbsolutePath());
                            jl5.setText("已完成 "+(finished));
                        }
                        dtde.dropComplete(true);
                    }else {
                        dtde.rejectDrop();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (UnsupportedFlavorException ufe) {
                    ufe.printStackTrace();
                }
            }
        });
    }
    public void rename(String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        File file = new File(dir.getAbsolutePath()+"\\"+name);
                        if(file.isDirectory()){
                            rename(file.getAbsolutePath());
                            return false;
                        }
                        if(file.isHidden()){
                            return false;
                        }
                        count();
                        return true;
                    }
                };
                File file = new File(filePath);
                String[] arr = file.list(filter);
                List<String> list = new ArrayList<>();
                for(String name:arr){
                    list.add(name);
                }
                sortFileName(list);

                int numberLength=numberFormat.lastIndexOf("#")-numberFormat.indexOf("#")+1;
                String prefix=numberFormat.substring(0,numberFormat.indexOf("#"));
                String suffix=numberFormat.substring(numberFormat.lastIndexOf("#")+1);
                List<File> tmpList=new ArrayList<>();
                for(int i=0;i<list.size();i++){
                    String numberStr=String.format("%0"+numberLength+"d",i+begin);
                    String oldPath=filePath+"\\"+list.get(i);
                    String newPath=filePath+"\\"+prefix+numberStr+suffix+list.get(i).substring(list.get(i).lastIndexOf("."));
                    File source=new File(oldPath);
                    File dest=new File(newPath);
                    if(source.getName().equals(dest.getName())){
                        finish();
                        jl5.setText("已完成："+finished+"/"+total);
                        continue;
                    }else if(dest.exists()){
                        File tmpFile=new File(newPath+".tmp");
                        source.renameTo(tmpFile);
                        tmpList.add(tmpFile);
                    }else{
                        source.renameTo(dest);
                        finish();
                        jl5.setText("已完成："+finished+"/"+total);
                    }
                }
                for(File f : tmpList){
                    f.renameTo(new File(f.getAbsolutePath().substring(0,f.getAbsolutePath().lastIndexOf(".tmp"))));
                    finish();
                    jl5.setText("已完成："+finished+"/"+total);
                }
                //jl5.setText("共完成"+(finished));
            }


        }).start();


    }
    public synchronized void finish(){
        finished++;
    }
    public synchronized  void count(){
        total++;
    }
    public static void main(String []args){
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        new MainFrame().setVisible(true);
    }

    public static void sortFileName(List<String> list){

        Collections.sort(list, new Comparator<String>() {
            //将字符串进行分割，将间隔的数字和字符串依次提取并放到list中
            protected List<String> split(String s){
                s = s.substring(0, s.lastIndexOf("."));
                List<String> list = new ArrayList<String>();
                char [] cs = s.toCharArray();
                int tmp = -1;
                for(int i=0;i<cs.length;i++){
                    char c = cs[i];
                    if(Character.isDigit(c)){
                        if(tmp < 0){
                            tmp =i;
                        }
                    }else{
                        if(tmp >= 0){
                            list.add(s.substring(tmp,i));
                            tmp =-1;
                        }
                        list.add(String.valueOf(c));                    }
                }
                if(Character.isDigit(cs[cs.length-1])){
                    tmp = tmp < 0 ? cs.length-1 : tmp;
                    list.add(s.substring(tmp,cs.length));
                    tmp = -1;
                }
                return list;
            }
            //当两个字符串字母部分相等，数字部分的值也相等时，比较数字字符串的位数
            private int compareNumberPart(String s1,String s2){
                int r=0;
                String[] ss1 = s1.split("\\D+");
                String[] ss2 = s2.split("\\D+");
                int len = ss1.length < ss2.length ? ss1.length :ss2.length ;
                for(int i=0;i<len; i++) {
                    r = compareValueEqualNumber(ss1[i], ss2[i]);
                    if (r != 0) {
                        return r;
                    }
                }
                return ss1.length > ss2.length ? 1 : -1;
            }
            //比较数字的大小
            private int compareNumber(String s1,String s2){
                int max = String.valueOf(Integer.MAX_VALUE).length()-1;
                int r = 0;
                if(s1.length() > max ||s2.length() >max){
                    r=new BigInteger(s1).compareTo(new BigInteger(s2));
                }else {
                    r=Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
                }
                return r;

            }
            //当数字大小相等时，比较数字的位数
            private int compareValueEqualNumber(String s1,String s2){
                int r=0;
                if(s1.length()>s2.length()){
                    return -1;
                }else if(s1.length() < s2.length()){
                    return 1;
                }else{
                    return 0;
                }
            }
           //重写比较规则
            @Override
            public int compare(String o1, String o2) {
                //windows下文件名称不区分大小写
                List<String> ss1 = split(o1.toLowerCase());
                List<String> ss2=split(o2.toLowerCase());
                //取分割后数组长度较小的长度；
                int len = ss1.size() < ss2.size() ? ss1.size() :ss2.size();
                int r=0;
                String t1 = null;
                String t2 = null;

                boolean b1 = false;
                boolean b2 = false;

                for(int i=0;i<len;i++){
                    //取依次取两个list中相同索引处 的字符串
                    t1 = ss1.get(i);
                    t2 = ss2.get(i);
                    //判断两个字符串是否是数字
                    b1 = Character.isDigit(t1.charAt(0));
                    b2 = Character.isDigit(t2.charAt(0));
                    //排序规则：1.数字 < 字母
                    if(b1 && !b2){
                        return -1;
                    }

                    if(!b1 && b2){
                        return 1;
                    }
                    //排序规则：2.对于两个非数字的字符串，按字符串比较规则进行
                    if(!b1 && !b2){
                        r=t1.compareTo(t2);
                        if(r != 0){
                            return r;
                        }
                    }
                    //排序规则：3.两个数字进行比较，比较值的大小
                    if(b1 && b2){
                        r=compareNumber(t1,t2);
                        if(r != 0){
                            return r;
                        }
                    }
                }

                //排序规则：4.比较 a_001+1 和 a_1 时，提取出其中的数字，依次比较数字的长度，长度大的较小，即 a_001+1 < a_1
                //排序规则：5.在比较a_001+1和a_001时，以上结果为零，现在比较里面所含数字的位数，a_001+1包含两个数字（001,1），a_001包含一个（001）， 数字多的较大

                if(r == 0){
                    r = compareNumberPart(o1.substring(0,o1.lastIndexOf(".")).toLowerCase(),o2.substring(0,o2.lastIndexOf(".")).toLowerCase());
                }
                return r;
            }
        });
    }
}
