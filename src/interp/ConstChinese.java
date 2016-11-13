package interp;

class ConstChinese {

    static final String NameOfPreviousValue = "*前者*";

    static final String ConsolePrompt = "解释器";

    static final String SystemFinishedPrompt = "系统中止并输出";

    static final String ErrorPrefix = "*** 系统错误: %s";
    static final String ErrorNotFoundSymbol = "未找到符号 %s";
    static final String ErrorNotFoundFile = "未找到文件 %s";
    static final String ErrorNotMatch = "模式不匹配";
    static final String ErrorSyntaxTooLittle = "输入代码太少";
    static final String ErrorSyntaxTooMuch = "输入代码太多";
    static final String ErrorSyntaxUndefined = "未定意常量 %s";
    static final String ErrorSyntaxIncorrectEscape = "非法转义序列";
    static final String ErrorCircleInsert = "列表不能插入到自身";
    static final String ErrorNameAlreadyExist = "符号已被定意或导出了 %s";
    static final String ErrorArgsAmount = "输入参数数目不合 %s 在 %s";
    static final String ErrorType = "类型错误";
    static final String ErrorExternalSymbol = "禁止更改外部符号 %s";

    static final String ImportNameSeparator = "";

    static final String ExprType = ":算式";
    static final String ListType = ":列表";
    static final String NumberType = ":数字";
    static final String StringType = ":文字";
    static final String BlobType = ":字节";
    static final String ExceptionType = ":异常";
    static final String HandleType = ":句柄";
    static final String NoneType = ":空";
    static final String SymbolType = ":符号";
    static final String TypeType = ":类型";
    static final String BoolType = ":真假";
    static final String LambdaType = ":算法";

    static final String Main = "入口";
    static final String MainArgs = "入口参数";

    static final String ValueOfLambdaType = "组合算法";
    static final String ValueOfListType = "[]";
    static final String ValueOfExprType = "()";
    static final String ValueOfNoneType = "#空";
    static final String ValueOfBoolTypeTrue = "#真";
    static final String ValueOfBoolTypeFalse = "#假";

    static final String LambdaToStringFormat = "(算法 %s %s)";

    static final String BoundArgs = "#参数";
    static final String BoundLambda = "#算法";

    static final String Define = "定意";
    static final String Update = "更新";

    static final String Cond = "选择";
    static final String Eq = "等于";
    static final String EqOp = "=";
    static final String Lambda = "算法";
    static final String Progn = "顺序";
    static final String If = "如若";
    static final String Apply = "施用";
    static final String Quote = "引用";
    static final String Let = "使";
    static final String Match = "匹配";
    static final String Import = "导入";
    static final String Export = "导出";
    static final String Eval = "求值";
    static final String Type = "类型";
    static final String Length = "长度";
    static final String Assert = "断言";
    static final String Exit = "退出";

    static final String Input = "输入";
    static final String Output = "输出";

    static final String Get = "取";
    static final String Set = "改";
    static final String Insert = "插入";
    static final String Delete = "删除";
    static final String Copy = "复制";

    static final String Substr = "片段";
    static final String Concat = "拼接";
    static final String Encode = "编码";
    static final String Decode = "解码";

    static final String And = "与";
    static final String Or = "或";
    static final String Not = "非";

    static final String Add = "加";
    static final String AddOp = "+";
    static final String Sub = "减";
    static final String SubOp = "-";
    static final String Mul = "乘";
    static final String MulOp = "*";
    static final String Div = "除";
    static final String DivOp = "/";
    static final String Mod = "余";
    static final String ModOp = "%";

    static final String Gt = "大于";
    static final String GtOp = ">";
    static final String Ge = "大于等于";
    static final String GeOp = ">=";
    static final String Lt = "小于";
    static final String LtOp = "<";
    static final String Le = "小于等于";
    static final String LeOp = "<=";
}
