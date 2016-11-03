package interp;

class ConstChinese {

    public static final String Help = "interp 0.1.0\n\n" +

            "interp        -> 进入交互模式\n" +
            "interp 文件名 -> 对文件求值";

    public static final String NameOfPreviousValue = "*前者*";

    public static final String ConsolePrompt = "解释器";

    public static final String SystemFinishedPrompt = "系统中止并输出";

    public static final String ErrorPrefix = "*** 系统错误: %s";
    public static final String AssertPrefix = "*** 命中: %s";
    public static final String ErrorNotImplemented = "功能未实现";
    public static final String ErrorNotFoundSymbol = "未找到符号 %s";
    public static final String ErrorNotFoundFile = "未找到文件 %s";
    public static final String ErrorNotMatch = "模式不匹配";
    public static final String ErrorSyntaxTooLittle = "输入代码太少";
    public static final String ErrorSyntaxTooMuch = "输入代码太多";
    public static final String ErrorSyntaxUndefined = "未定意常量 %s";
    public static final String ErrorSyntaxIncorrectEscape = "非法转义序列";
    public static final String ErrorCircleInsert = "列表不能插入到自身";
    public static final String ErrorNameAlreadyExport = "符号已被导出了 %s";
    public static final String ErrorArgsAmount = "输入参数数目不合 %s 在 %s";
    public static final String ErrorType = "类型错误";

    public static final String ImportNameSeparator = "";

    public static final String ExprType = ":算式";
    public static final String ListType = ":列表";
    public static final String NumberType = ":数字";
    public static final String StringType = ":文字";
    public static final String BlobType = ":字节";
    public static final String ExceptionType = ":异常";
    public static final String HandleType = ":句柄";
    public static final String NoneType = ":空";
    public static final String SymbolType = ":符号";
    public static final String TypeType = ":类型";
    public static final String BoolType = ":真假";
    public static final String LambdaType = ":算法";

    public static final String Main = "入口";

    public static final String ValueOfLambdaType = "组合算法";
    public static final String ValueOfListType = "[]";
    public static final String ValueOfExprType = "()";
    public static final String ValueOfNoneType = "#空";
    public static final String ValueOfBoolTypeTrue = "#真";
    public static final String ValueOfBoolTypeFalse = "#假";

    public static final String LambdaToStringFormat = "(算法 %s %s)";
    public static final String QuoteToStringFormat = "(解码 %s)";


    public static final String BoundArgs = "#参数";
    public static final String BoundLambda = "#算法";

    public static final String Define = "定意";
    public static final String Update = "更新";

    public static final String Cond = "选择";
    public static final String Eq = "等于";
    public static final String Lambda = "算法";
    public static final String Progn = "顺序";
    public static final String If = "如若";
    public static final String Apply = "施用";
    public static final String Quote = "引用";
    public static final String Let = "使";
    public static final String Match = "匹配";
    public static final String Import = "导入";
    public static final String Export = "导出";
    public static final String Eval = "求值";
    public static final String Type = "类型";
    public static final String Exit = "退出";

    public static final String Input = "输入";
    public static final String Output = "输出";

    public static final String Assert = "断言";
    public static final String Trap = "自陷";

    public static final String Get = "取";
    public static final String Set = "改";
    public static final String Insert = "插入";
    public static final String Delete = "删除";
    public static final String Copy = "复制";

    public static final String Substr = "片段";
    public static final String Concat = "拼接";

    public static final String Length = "长度";

    public static final String Encode = "编码";
    public static final String Decode = "解码";

    public static final String And = "与";
    public static final String Or = "或";
    public static final String Not = "非";

    public static final String Add = "加";
    public static final String AddOp = "+";
    public static final String Sub = "减";
    public static final String SubOp = "-";
    public static final String Mul = "乘";
    public static final String MulOp = "*";
    public static final String Div = "除";
    public static final String DivOp = "/";
    public static final String Mod = "余";
    public static final String ModOp = "%";

    public static final String Gt = "大于";
    public static final String GtOp = ">";
    public static final String Ge = "大于等于";
    public static final String GeOp = ">=";
    public static final String Lt = "小于";
    public static final String LtOp = "<";
    public static final String Le = "小于等于";
    public static final String LeOp = "<=";
}
