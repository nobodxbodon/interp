package interp;

class ConstEnglish {

    public static final String Help = "interp 0.1.0\n\n" +

            "interp             -> into interactive mode\n" +
            "interp a-file-name -> evaluate file";

    public static final String NameOfPreviousValue = "_";

    public static final String ConsolePrompt = "REPL";

    public static final String SystemFinishedPrompt = "System finished with";

    public static final String ErrorPrefix = "*** System Error: %s";
    public static final String ErrorNotImplemented = "Not implemented yet";
    public static final String ErrorNotFoundSymbol = "Not found symbol '%s'";
    public static final String ErrorNotFoundFile = "Not found file %s";
    public static final String ErrorNotMatch = "Match not match";
    public static final String ErrorSyntaxTooLittle = "Too little input";
    public static final String ErrorSyntaxTooMuch = "Too many input";
    public static final String ErrorSyntaxUndefined = "Undefined literal %s";
    public static final String ErrorSyntaxIncorrectEscape = "Incorrect escape";
    public static final String ErrorCircleInsert = "Can not insert into one's itself";
    public static final String ErrorNameAlreadyExport = "Already export, '%s'";
    public static final String ErrorArgsAmount = "Arguments amount error, '%s' @ %s";
    public static final String ErrorType = "Incorrect type";

    public static final String ImportNameSeparator = "-";

    public static final String ExprType = ":expr";
    public static final String ListType = ":list";
    public static final String NumberType = ":number";
    public static final String StringType = ":string";
    public static final String BlobType = ":blob";
    public static final String ExceptionType = ":exception";
    public static final String HandleType = ":handle";
    public static final String NoneType = ":none";
    public static final String SymbolType = ":symbol";
    public static final String TypeType = ":type";
    public static final String BoolType = ":bool";
    public static final String LambdaType = ":lambda";

    public static final String Main = "main";

    public static final String ValueOfLambdaType = "__lambda__";
    public static final String ValueOfListType = "[]";
    public static final String ValueOfExprType = "()";
    public static final String ValueOfNoneType = "#none";
    public static final String ValueOfBoolTypeTrue = "#true";
    public static final String ValueOfBoolTypeFalse = "#false";

    public static final String LambdaToStringFormat = "(lambda %s %s)";
    public static final String QuoteToStringFormat = "(decode %s)";


    public static final String BoundArgs = "#args";
    public static final String BoundLambda = "#lambda";

    public static final String Define = "define";
    public static final String Update = "update";

    public static final String Cond = "cond";
    public static final String Eq = "eq";
    public static final String Lambda = "lambda";
    public static final String Progn = "progn";
    public static final String If = "if";
    public static final String Apply = "apply";
    public static final String Quote = "quote";
    public static final String Let = "let";
    public static final String Match = "match";
    public static final String Import = "import";
    public static final String Export = "export";
    public static final String Eval = "eval";
    public static final String Type = "type";
    public static final String Exit = "exit";

    public static final String Input = "input";
    public static final String Output = "output";

    public static final String Assert = "assert";
    public static final String Trap = "trap";

    public static final String Get = "get";
    public static final String Set = "set";
    public static final String Insert = "insert";
    public static final String Delete = "delete";
    //    public static final String Length = "length";
    public static final String Copy = "copy";

    public static final String Substr = "substr";
    public static final String Concat = "concat";
    public static final String Length = "length";
    public static final String Encode = "encode";
    public static final String Decode = "decode";

    public static final String And = "and";
    public static final String Or = "or";
    public static final String Not = "not";

    public static final String Add = "add";
    public static final String AddOp = "+";
    public static final String Sub = "sub";
    public static final String SubOp = "-";
    public static final String Mul = "mul";
    public static final String MulOp = "*";
    public static final String Div = "div";
    public static final String DivOp = "/";
    public static final String Mod = "mod";
    public static final String ModOp = "%";

    public static final String Gt = "gt";
    public static final String GtOp = ">";
    public static final String Ge = "ge";
    public static final String GeOp = ">=";
    public static final String Lt = "lt";
    public static final String LtOp = "<";
    public static final String Le = "le";
    public static final String LeOp = "<=";
}
