package lmodeler;

/**
 *Trida pro vyvolani okna manualu. Obsahuje i text.
 * @author Jakub Kvita
 */
public class HelpFrame extends javax.swing.JFrame {
    
    private final String htmlText1 = "<font face=\"tahoma\"size=4><br><br><center><font size=6><b>L-systém</b></font></center><br><br>"
            + "L-systém je druh formální gramatiky, vyvinutý pro modelování rostlin a jejich růstu."
            + " Jako jiné gramatiky se L-systém skládá z abecedy symbolů, řetezce těchto symbolů popisujícího model"
            + " =<i><b>axiom</b></i> (druhé políčko shora) a pravidel, která opakovaně přepisují symboly tohoto řetězce.<br><br>"
            + "Při aplikaci pravidel na řetězec se přepisují všechny symboly paralelně a model se dostává do určitých úrovní."
            + " Tato akce se nazývá derivace. Pro přesun mezi úrovněmi slouží prvky na posledních dvou řádcích dole v ovladači vlevo.<br>"
            + "Pole <i><b>Ignorované symboly</b></i> slouží pro další nastavení u některých pravidel.<br><br>"
            + "Aby text napsaný do polí <i><b>Ignorované symboly</b></i>, <i><b>Axiom</b></i> a <i><b>Pravidla</b></i>,"
            + " začal účinkovat, je potřeba stisknout tlačítko <i><b>Načíst systém</b></i>.";
    private final String htmlText2 = "<font face=\"tahoma\"size=4><br><br><center><font size=6><b>Pravidla</b></font></center><br><br>"
            + "Všechna pravidla mají stejný tvar, ale je možné některé části vypustit, takže se mohou zjednodušit."
            + " Základní tvar pravidla je:<br><center> SYMBOL  <b>=</b>  ŘETĚZEC,</center>kde <b>=</b> je znak,"
            + " který se musí vyskytovat a SYMBOL a ŘETĚZEC zastupují jakékoli symboly abecedy.<br><br>"
            + "Dále je možno hledat kontext přepisovaného symbolu a přepisovat pouze některé symboly. Zvlášť lze použít pravý i levý kontext ve tvaru:<br>"
            + "<center> ŘETĚZEC  <b>&lt;</b>  SYMBOL  <b>&gt;</b>  ŘETĚZEC  <b>=</b>  ŘETĚZEC.</center><br>"
            + "Poslední volitelnou částí je možnost přidání pravděpodobnosti s jakou se pravidlo použije:<br>"
            + "<center> SYMBOL  <b>=</b>  ČÍSLO  <b>=</b>  ŘETĚZEC,</center>kde číslo je číslo od 0 do 1 s tím,"
            + " že pravidla aplikující se na stejnou položku musí mít celkový součet pravděpodobnosti 1."
            + " Pokud u některých pravidel není uvedena, tak se dopočítá a přiřadí jim stejně velkou.";
    private final String htmlText3 = "<font face=\"tahoma\"size=4><br><br><center><font size=6><b>Další nastavení</b></font></center><br><br>"
            + "Aby bylo možné řetězec L-systému smysluplně interpretovat je potřeba zavést položku jak moc se má zatočit při zatáčení."
            + " K tomu slouží pole <b>Úhel otočení</b>, který je možno specifikovat v celých stupních od 0 do 359.<br><br>"
            + "Pro lepší vizualizaci je možno nastavit dva druhy barevých sad v poli <b>Barevná sada</b>."
            + " <i>Základní</i> znamená, že je zde 11 barev - bílá, červená, oranžová, žlutá, zelená, azurová, modrá, purpurová, růžová, šedá a hnědá."
            + " V <i>rozšířené</i> sadě je k dispozici celé barevné spektrum s celkem 500 barvami, které stačí pro veškerou činnost.<br><br>"
            + "Poslední možností nastavení je manipulace s tloušťkou segmentů v poli <b>Počáteční tloušťka</b>"
            + " a v poli <b>Koeficient tloušťky</b>, které určuje zeštíhlení segmentů po aplikaci symbolu !.<br>"
            + " Délka segmentu je 1 jednotka. Tímto je možno modelovat druhy stromů s různým poměrem délky a tloušťky.";
    private final String htmlText4 = "<font face=\"tahoma\"size=4><br><br><center><font size=6><b>Možnosti menu</b></font></center><br><br>"
            + "Jednotlivé systémy je možno ukládat do souboru a načítat z něj."
            + " Uloženy jsou ve formátu xml, který lze číst i prostým okem a jsou uloženy tak, aby je bylo možné upravovat i bez spuštění programu.<br><br>"
            + "Dále je možno spustit velké množství ukázkových systémů, které jsou už integrovány v programu, a dostat se k tomuto manuálu.";
    private final String htmlText5 = "<font face=\"tahoma\"size=4><br><br><center><font size=6><b>Interpretace symbolů</b></font></center><br><br>"
            + "Symboly jsou interpretovány podle pravidel želví grafiky a to následovně:<br><br>"
            + "<b>A</b> - <b>P</b> ..... Želva udělá krok dopředu a nakreslí čáru.<br>"
            + "<b>Q</b> - <b>Z</b> ..... Nic<br>"
            + "<b>a</b> - <b>p</b> ..... Želva udělá krok dopředu bez kreslení čáry.<br>"
            + "<b>q</b> - <b>z</b> ..... Nic<br>"
            + "<b>  +</b>   ..... Želva se otočí doleva.<br>"
            + "<b>  -</b>   ..... Želva se otočí doprava.<br>"
            + "<b>  ^</b>   ..... Želva zvedne hlavu.<br>"
            + "<b>  &amp;</b>   ..... Želva skloní hlavu.<br>"
            + "<b>  \\</b>   ..... Želva se převalí doleva.<br>"
            + "<b>  /</b>   ..... Želva se převalí doprava.<br>"
            + "<b>  |</b>   ..... Želva se otočí čelem vzad.<br>"
            + "<b>  [</b>   ..... Ulož stav želvy na zásobník a začni vytvářet větev.<br>"
            + "<b>  ]</b>   ..... Ukonči vytváření větve a načti stav želvy ze zásobníku.<br>"
            + "<b>  '</b>   ..... Posuň ukazatel barvy o jedna nahoru.<br>"
            + "<b>  ,</b>   ..... Posuň ukazatel barvy o jedna dolů.<br>"
            + "<b>  {</b>   ..... Začni vytvářet polygon.<br>"
            + "<b>  }</b>   ..... Ukonči vytváření polygonu a vykresli ho.<br>"
            + "<b>  .</b>   ..... Ulož současnou pozici jako vrchol polygonu.<br>"
            + "<b>  !</b>   ..... Zmenši šířku vykreslovaných segmentů.<br>"
            + "<b>  %</b>   ..... Odstraň všechny symboly od tohoto až do konce větve.<br>";    
    
    /**
     * Konstruktor.
     */
    public HelpFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenu1 = new javax.swing.JMenu();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextPane5 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane4 = new javax.swing.JTextPane();

        jMenu1.setText("jMenu1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Manuál");
        setResizable(false);

        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jScrollPane1.setViewportView(jTextPane1);
        jTextPane1.setEditable(false);
        jTextPane1.setContentType("text/html");
        jTextPane1.setText(htmlText1);

        jTabbedPane1.addTab("   L-systém   ", jScrollPane1);

        jScrollPane5.setViewportView(jTextPane5);
        jTextPane5.setEditable(false);
        jTextPane5.setContentType("text/html");
        jTextPane5.setText(htmlText5);

        jTabbedPane1.addTab("Interpretace symbolů", jScrollPane5);

        jScrollPane2.setViewportView(jTextPane2);
        jTextPane2.setEditable(false);
        jTextPane2.setContentType("text/html");
        jTextPane2.setText(htmlText2);

        jTabbedPane1.addTab("   Pravidla   ", jScrollPane2);

        jScrollPane3.setViewportView(jTextPane3);
        jTextPane3.setEditable(false);
        jTextPane3.setContentType("text/html");
        jTextPane3.setText(htmlText3);

        jTabbedPane1.addTab("   Další nastavení   ", jScrollPane3);

        jScrollPane4.setViewportView(jTextPane4);
        jTextPane4.setEditable(false);
        jTextPane4.setContentType("text/html");
        jTextPane4.setText(htmlText4);

        jTabbedPane1.addTab("   Možnosti menu   ", jScrollPane4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 495, Short.MAX_VALUE)
        );

        jTabbedPane1.getAccessibleContext().setAccessibleName("");

        getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextPane jTextPane2;
    private javax.swing.JTextPane jTextPane3;
    private javax.swing.JTextPane jTextPane4;
    private javax.swing.JTextPane jTextPane5;
    // End of variables declaration//GEN-END:variables
}
