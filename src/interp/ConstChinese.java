package interp;

class ConstChinese {

    static final String NameOfPreviousValue = "__";

    static final String ConsolePrompt = "解釋器";

    static final String SystemFinishedPrompt = "系統中止並輸出";

    static final String ErrorPrefix = "*** 系統錯誤: %s";
    static final String ErrorNotFoundSymbol = "未找到符號 %s";
    static final String ErrorNotFoundFile = "未找到文件 %s";
    static final String ErrorNotMatch = "模式不匹配";
    static final String ErrorSyntaxTooLittle = "輸入代碼太少";
    static final String ErrorSyntaxTooMuch = "輸入代碼太多";
    static final String ErrorSyntaxUndefined = "未定意常量 %s";
    static final String ErrorSyntaxIncorrectEscape = "非法轉譯序列";
    static final String ErrorCircleInsert = "列表不能插入自身";
    static final String ErrorNameAlreadyExist = "符號已經被定意或導出了 %s";
    static final String ErrorArgsAmount = "輸入參數數目錯誤 %s 在 %s";
    static final String ErrorType = "類型錯誤";
    static final String ErrorExternalSymbol = "禁止更改外部符號 %s";

    static final String ImportNameSeparator = "";

    static final String ExprType = ":算式";
    static final String ListType = ":列表";
    static final String NumberType = ":數字";
    static final String StringType = ":文字";
    static final String BlobType = ":字節";
    static final String ExceptionType = ":異常";
    static final String HandleType = ":句柄";
    static final String NoneType = ":空";
    static final String SymbolType = ":符號";
    static final String TypeType = ":類型";
    static final String BoolType = ":真假";
    static final String LambdaType = ":算法";

    static final String Main = "入口";
    static final String MainArgs = "入口參數";

    static final String ValueOfLambdaType = "組合算法";
    static final String ValueOfListType = "[]";
    static final String ValueOfExprType = "()";
    static final String ValueOfNoneType = "#空";
    static final String ValueOfBoolTypeTrue = "#真";
    static final String ValueOfBoolTypeFalse = "#假";

    static final String LambdaToStringFormat = "(算法 %s %s)";

    static final String BoundArgs = "#參數";
    static final String BoundLambda = "#算法";

    static final String Define = "定意";
    static final String Update = "更新";

    static final String Cond = "選擇";
    static final String Eq = "等於";
    static final String EqOp = "=";
    static final String Lambda = "算法";
    static final String Progn = "順序";
    static final String If = "如若";
    static final String Apply = "施用";
    static final String Quote = "引用";
    static final String Let = "使";
    static final String Match = "匹配";
    static final String Import = "導入";
    static final String Export = "導出";
    static final String Eval = "求值";
    static final String Type = "類型";
    static final String Length = "長度";
    static final String Assert = "斷言";
    static final String Exit = "退出";

    static final String Input = "輸入";
    static final String Output = "輸出";

    static final String Get = "取";
    static final String Set = "改";
    static final String Insert = "插入";
    static final String Delete = "刪除";
    static final String Copy = "複製";

    static final String Substr = "片段";
    static final String Concat = "拼接";
    static final String Encode = "編碼";
    static final String Decode = "解碼";

    static final String And = "與";
    static final String Or = "或";
    static final String Not = "非";

    static final String Add = "加";
    static final String AddOp = "+";
    static final String Sub = "減";
    static final String SubOp = "-";
    static final String Mul = "乘";
    static final String MulOp = "*";
    static final String Div = "除";
    static final String DivOp = "/";
    static final String Mod = "餘";
    static final String ModOp = "%";

    static final String Gt = "大於";
    static final String GtOp = ">";
    static final String Ge = "大於等於";
    static final String GeOp = ">=";
    static final String Lt = "小於";
    static final String LtOp = "<";
    static final String Le = "小於等於";
    static final String LeOp = "<=";
}
