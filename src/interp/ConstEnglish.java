package interp;

class ConstEnglish {

    static final String NameOfPreviousValue = "_";

    static final String ConsolePrompt = "REPL";

    static final String SystemFinishedPrompt = "System finished with";

    static final String ErrorPrefix = "*** System Error: %s";
    static final String ErrorNotFoundSymbol = "Not found symbol '%s'";
    static final String ErrorNotFoundFile = "Not found file '%s'";
    static final String ErrorNotMatch = "Match not match";
    static final String ErrorSyntaxTooLittle = "Too little input";
    static final String ErrorSyntaxTooMuch = "Too many input";
    static final String ErrorSyntaxUndefined = "Undefined literal '%s'";
    static final String ErrorSyntaxIncorrectEscape = "Incorrect escape";
    static final String ErrorCircleInsert = "Can not insert into one's itself";
    static final String ErrorNameAlreadyExist = "Name already exist '%s'";
    static final String ErrorArgsAmount = "Arguments amount error, '%s' @ %s";
    static final String ErrorType = "Incorrect type";
    static final String ErrorExternalSymbol = "Forbidden to update external name '%s'";

    static final String ImportNameSeparator = "-";

    static final String ExprType = ":expr";
    static final String ListType = ":list";
    static final String NumberType = ":number";
    static final String StringType = ":string";
    static final String BlobType = ":blob";
    static final String ExceptionType = ":exception";
    static final String HandleType = ":handle";
    static final String NoneType = ":none";
    static final String SymbolType = ":symbol";
    static final String TypeType = ":type";
    static final String BoolType = ":bool";
    static final String LambdaType = ":lambda";

    static final String Main = "main";
    static final String MainArgs = "args";

    static final String ValueOfLambdaType = "__lambda__";
    static final String ValueOfListType = "[]";
    static final String ValueOfExprType = "()";
    static final String ValueOfNoneType = "#none";
    static final String ValueOfBoolTypeTrue = "#true";
    static final String ValueOfBoolTypeFalse = "#false";

    static final String LambdaToStringFormat = "(lambda %s %s)";

    static final String BoundArgs = "#args";
    static final String BoundLambda = "#lambda";

    static final String Define = "define";
    static final String Update = "update";

    static final String Cond = "cond";
    static final String Eq = "eq";
    static final String EqOp = "=";
    static final String Lambda = "lambda";
    static final String Progn = "progn";
    static final String If = "if";
    static final String Apply = "apply";
    static final String Quote = "quote";
    static final String Let = "let";
    static final String Match = "match";
    static final String Import = "import";
    static final String Export = "export";
    static final String Eval = "eval";
    static final String Type = "type";
    static final String Length = "length";
    static final String Assert = "assert";
    static final String Exit = "exit";

    static final String Input = "input";
    static final String Output = "output";

    static final String Get = "get";
    static final String Set = "set";
    static final String Insert = "insert";
    static final String Delete = "delete";
    static final String Copy = "copy";

    static final String Substr = "substr";
    static final String Concat = "concat";
    static final String Encode = "encode";
    static final String Decode = "decode";

    static final String And = "and";
    static final String Or = "or";
    static final String Not = "not";

    static final String Add = "add";
    static final String AddOp = "+";
    static final String Sub = "sub";
    static final String SubOp = "-";
    static final String Mul = "mul";
    static final String MulOp = "*";
    static final String Div = "div";
    static final String DivOp = "/";
    static final String Mod = "mod";
    static final String ModOp = "%";

    static final String Gt = "gt";
    static final String GtOp = ">";
    static final String Ge = "ge";
    static final String GeOp = ">=";
    static final String Lt = "lt";
    static final String LtOp = "<";
    static final String Le = "le";
    static final String LeOp = "<=";
}
