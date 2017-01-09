package interp;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static interp.Const.*;

public class Interp {

    public static void main(String[] args) {

        if (args.length == 0) {

            Scanner inScanner = new Scanner(System.in);
            String in;
            Node out;

            while (true) {

                Env env = new Env();
                env.define(NameOfPreviousValue, Node.createNoneNode());
                LibInterp.aio("(import \"/utils.l\")", env);

                while (true) {

                    System.out.printf("[%s]<<< ", ConsolePrompt);

                    in = inScanner.nextLine();

                    if (in.isEmpty()) continue;
                    if (in.equalsIgnoreCase("$reload")) {
                        System.out.println("\n=========== Reloaded ===========\n");
                        break;
                    }

                    try {

                        out = LibInterp.aio(in, env);

                        env.update(NameOfPreviousValue, out);

                        System.out.printf("[%s]>>> ", ConsolePrompt);
                        System.out.println(out);

                    } catch (InterpSystemError | InterpSystemAssert e) {

                        System.out.println(e.getMessage());

                    } catch (InterpSystemExit interpSystemExit) {

                        System.exit(0);

                    } finally {

                        System.out.println();
                    }
                }
            }

        } else {


            String filePath = args[0];

            Node retval = Node.createNoneNode();

            try {

                retval = LibInterp.runMain(filePath, args);

            } catch (InterpSystemError e) {

                System.out.println(e.getMessage());
                e.printStackTrace();

            } catch (InterpSystemExit interpSystemExit) {

                System.exit(0);
            }

            System.out.printf("\n%s %s", SystemFinishedPrompt, retval);

        }
    }
}

class LibInterp {

    private static final Stack<Path> ImportedPathStack = new Stack<>();
    private static final Path LibraryRoot;

    static {
        ImportedPathStack.push(Paths.get(".").toAbsolutePath().normalize());
        LibraryRoot = Paths.get("/home/ner0/IdeaProjects/interp/lib/");
    }

    public static Node aio(String code, Env env) {

        return eval(parse(lex(clean(code))), env);
    }

    public static Node runMain(String filePath, String[] args) {

        List<Node> Args = new LinkedList<>();

        for (int i = 1; i < args.length; i++) {
            Args.add(Node.createStringNode(args[i]));
        }

        Env env = new Env();
        env.define(MainArgs, Node.createListNode(Args));

        return aio(String.format("(%s (%s \"%s\") (%s %s))", Progn, Import, filePath, Main, MainArgs), env);
    }

    public static String progn(String s) {

        return String.format("(%s %s)", Progn, s);
    }

    public static String clean(String code) {

        StringBuilder builder = new StringBuilder();

        String status = "";

        char c;

        for (int i = 0; i < code.length(); i++) {

            c = code.charAt(i);

            switch (status) {

                case "quote":

                    if (c == '"') {
                        status = "";
                        builder.append(c);
                    } else {
                        builder.append(c);
                    }
                    break;

                case "comment":

                    if (c == '\n') {
                        status = "";
                        builder.append(c);
                    } else { /* pass comment char*/ }
                    break;

                default:

                    if (c == '"') {
                        status = "quote";
                        builder.append(c);
                    } else if (c == ';') {
                        status = "comment";
                    } else {
                        builder.append(c);
                    }
                    break;
            }
        }

        return builder.toString();
    }

    public static List<String> lex(String code) {

        List<String> fragments = new LinkedList<>();

        String pattern = "\\(|\\)|\\[|\\]|\".*?\"|'.*?'|(?<=\\(|\\)|\\[|\\]|\\s|\"|'|^)\\S+?(?=\\(|\\)|\\[|\\]|\\s|\"|'|$)";
        Matcher matcher = Pattern.compile(pattern).matcher(code);

        while (matcher.find()) {

            fragments.add(code.substring(matcher.start(), matcher.end()));
        }

        return fragments;
    }

    private static String __escape(String s) {

        StringBuilder builder = new StringBuilder();
        int i = 0;

        char c;

        while (i < s.length()) {

            c = s.charAt(i);

            if (c == '%') {
                if (i + 1 == s.length()) {
                    throw new InterpSystemError(ErrorSyntaxIncorrectEscape);
                } else if (s.charAt(i + 1) == '%') {
                    builder.append(c);
                    i += 2;
                } else if (s.charAt(i + 1) == 'n') {
                    builder.append('\n');
                    i += 2;
                } else if (s.charAt(i + 1) == 'q') {
                    builder.append('"');
                    i += 2;
                } else {
                    throw new InterpSystemError(ErrorSyntaxIncorrectEscape);
                }
            } else {
                builder.append(c);
                i++;
            }

        }

        return builder.toString();
    }

    private static class ParseRollback extends RuntimeException {
    }

    private static Node __parse(List<String> L) {

        if (L.isEmpty()) throw new InterpSystemError(ErrorSyntaxTooLittle);

        String s = L.remove(0);
        Node node;

        switch (s) {
            case "(":
                node = new Node(ExprType, ValueOfExprType);
                try {
                    while (true) node.addSubNode(__parse(L));
                } catch (ParseRollback e) {
                    return node;
                }

            case "[":
                node = new Node(ListType, ValueOfListType);
                try {
                    while (true) node.addSubNode(__parse(L));
                } catch (ParseRollback e) {
                    return node;
                }

            case ")":
            case "]":
                throw new ParseRollback();

            default:
                return __identify(s);
        }
    }

    private static Node __identify(String s) {

        if (s.matches("^[+-]?\\d+(\\.\\d+([Ee][+-]?\\d+)?)?$")) {

            return new Node(NumberType, new BigDecimal(s));

        } else if (s.startsWith("\"") && s.endsWith("\"")) {

            return new Node(StringType, __escape(s.substring(1, s.length() - 1)));

        } else if (s.startsWith("'") && s.endsWith("'")) {

            return new Node(BlobType, s.substring(1, s.length() - 1));

        } else if (s.startsWith("&")) {

            return new Node(ExceptionType, s);

        } else if (s.startsWith("@")) {

            return new Node(HandleType, s);

        } else if (s.startsWith("#")) {

            switch (s) {

                case ValueOfBoolTypeTrue:
                    return new Node(BoolType, s);
                case ValueOfBoolTypeFalse:
                    return new Node(BoolType, s);
                case ValueOfNoneType:
                    return new Node(NoneType, s);
                case BoundArgs:
                    return new Node(SymbolType, s);
                case BoundLambda:
                    return new Node(SymbolType, s);

                default:
                    throw new InterpSystemError(String.format(ErrorSyntaxUndefined, s));
            }
        } else if (s.startsWith(":")) {

            switch (s) {

                case ExprType:
                case ListType:
                case NumberType:
                case StringType:
                case BlobType:
                case ExceptionType:
                case HandleType:
                case NoneType:
                case SymbolType:
                case TypeType:
                case BoolType:
                case LambdaType:
                    return new Node(TypeType, s);

                default:
                    throw new InterpSystemError(String.format(ErrorSyntaxUndefined, s));
            }
        } else {

            return new Node(SymbolType, s);
        }

    }

    public static Node parse(List<String> fragments) {

        Node node;

        try {
            node = __parse(fragments);
        } catch (ParseRollback e) {
            throw new InterpSystemError(ErrorSyntaxTooLittle);
        }

        if (!fragments.isEmpty()) {
            throw new InterpSystemError(ErrorSyntaxTooMuch);
        } else {
            return node;
        }
    }

    private static boolean __eq(int i, int j, List<Node> L, Env env) {

        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return eval(node_i, env).eq(eval(node_j, env)) && __eq(j, j + 1, L, env);
        }
    }

    private static boolean __match(Node param, Node pattern, Env env) {

        if (param.getSubNodesAmount() != pattern.getSubNodesAmount()) {
            return false;
        } else {
            Node i, j;
            for (int k = 0; k < param.getSubNodesAmount(); k++) {
                i = param.getSubNode(k);
                j = pattern.getSubNode(k);

                if (i.getType().equals(ListType) && j.getType().equals(ListType)) {
                    if (__match(i, j, env)) {
                        // pass
                    } else {
                        return false;
                    }
                } else {
                    if (j.getValueString().equals("")) {
                        // pass
                    } else {
                        env.define(j.getValueString(), i);
                    }
                }
            }

            return true;
        }

    }

    private static boolean __and(int i, int j, List<Node> L, Env env) {

        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return eval(node_i, env).getValueBool() && eval(node_j, env).getValueBool() && __and(j, j + 1, L, env);
        }
    }

    private static boolean __or(int i, int j, List<Node> L, Env env) {

        if (j == L.size()) {

            return false;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return (eval(node_i, env).getValueBool() || eval(node_j, env).getValueBool()) || __or(j, j + 1, L, env);
        }
    }

    private static List<Node> __eval_all(List<Node> L, Env env) {
        List<Node> _L = new LinkedList<>();

        for (Node n : L) _L.add(eval(n, env));

        return _L;
    }

    private static boolean __check_args_amount(String op, int amount) {

        switch (op) {
            case Define:
                return amount == 2;
            case Update:
                return amount == 2;

            case Cond:
                return amount >= 1;
            case Is:
                return amount == 2;
            case EqOp:
            case Eq:
                return amount >= 2;
            case Lambda:
                return amount == 2;
            case Progn:
                return amount >= 1;
            case If:
                return amount == 2 || amount == 3;
            case Apply:
                return amount == 2;
            case Quote:
                return amount == 1;
            case Let:
                return amount == 2;
            case Match:
                return amount == 3;
            case Import:
                return amount == 1 || amount == 2;
            case Export:
                return amount == 2;
            case Eval:
                return amount == 1;
            case Type:
                return amount == 1;
            case Exit:
                return amount == 0;
            case Input:
                return amount == 0;
            case Output:
                return amount >= 1;
            case Assert:
                return amount == 1;

            case Get:
                return amount == 2;
            case Set:
                return amount == 3;
            case Insert:
                return amount == 3;
            case Delete:
                return amount == 2;
            case Copy:
                return amount == 1;

            case Substr:
                return amount == 3;
            case Concat:
                return amount >= 2;
            case Length:
                return amount == 1;
            case Encode:
                return amount == 1;
            case Decode:
                return amount == 1;

            case And:
                return amount >= 2;
            case Or:
                return amount >= 2;
            case Not:
                return amount == 1;

            case Add:
            case AddOp:
                return amount >= 2;
            case Sub:
            case SubOp:
                return amount >= 2;
            case Mul:
            case MulOp:
                return amount >= 2;
            case Div:
            case DivOp:
                return amount >= 2;
            case Mod:
            case ModOp:
                return amount >= 2;

            case Gt:
            case GtOp:
                return amount >= 2;
            case Ge:
            case GeOp:
                return amount >= 2;
            case Lt:
            case LtOp:
                return amount >= 2;
            case Le:
            case LeOp:
                return amount >= 2;

            default:
                return true;
        }
    }

    public static Node eval(Node node, Env env) {

        Node retval = Node.createNoneNode();
        String type = node.getType();

        switch (type) {

            case SymbolType:

                retval = env.lookup(node.getValueString());
                break;

            case NumberType:
            case StringType:
            case BoolType:
            case NoneType:
            case LambdaType:
            case ExceptionType:
            case BlobType:
            case HandleType:
            case TypeType:

                retval = node;
                break;

            case ListType:

                List<Node> list = new LinkedList<>();

                for (Node subNode : node.getSubNodes()) {
                    list.add(eval(subNode, env));
                }

                retval = Node.createListNode(list);
                break;

            case ExprType:

                Node opNode = eval(node.getSubNode(0), env);

                if (!opNode.getType().equals(LambdaType)) throw new InterpSystemError(ErrorType);

                String op = opNode.getValueString();

                List<Node> L = node.getSubNodes().subList(1, node.getSubNodesAmount());

                if (op.equals(ValueOfLambdaType)) {

                    Node args = opNode.getSubNode(0);
                    Node expr = opNode.getSubNode(1);
                    Env env0 = opNode.getEnv().grow();

                    if (!(args.getSubNodesAmount() == 0 || L.size() == args.getSubNodesAmount()))
                        throw new InterpSystemError(String.format(ErrorArgsAmount, opNode, node));

                    List<Node> parameters = __eval_all(L, env);

                    for (int i = 0; i < args.getSubNodesAmount(); i++) {

                        env0.define(args.getSubNode(i).getValueString(), parameters.get(i));
                    }

                    env0.define(BoundArgs, Node.createListNode(parameters));
                    env0.define(BoundLambda, opNode);

                    retval = eval(expr, env0);

                } else {

                    if (!__check_args_amount(op, L.size()))
                        throw new InterpSystemError(String.format(ErrorArgsAmount, opNode, node));

                    switch (op) {

                        case Define:
                            retval = eval(L.get(1), env);
                            env.define(L.get(0).getValueString(), retval);
                            break;

                        case Update:

                            retval = eval(L.get(1), env);

                            try {

                                env.update(L.get(0).getValueString(), retval);

                            } catch (NoSuchElementException e) {

                                throw new InterpSystemError(String.format(ErrorNotFoundSymbol, L.get(0).getValueString()));
                            }
                            break;

                        case Cond:

                            retval = Node.createNoneNode();

                            for (Node n : L) {
                                if (eval(n.getSubNode(0), env).getValueString().equals(ValueOfBoolTypeTrue)) {
                                    retval = eval(n.getSubNode(1), env);
                                    break;
                                }
                            }
                            break;

                        case Is:
                            Node _a = eval(L.get(0), env);
                            Node _b = eval(L.get(1), env);
                            retval = Node.createBoolNode(_a == _b);
                            break;

                        case EqOp:
                        case Eq:

                            retval = Node.createBoolNode(__eq(0, 1, L, env));
                            break;

                        case Lambda:

                            retval = Node.createLambdaNode(L, env);
                            break;

                        case Progn:

                            for (Node n : L) retval = eval(n, env);
                            break;

                        case If:

                            if (eval(L.get(0), env).getValueBool()) {

                                retval = eval(L.get(1), env);

                            } else {

                                retval = (L.size() == 3) ? eval(L.get(2), env) : Node.createNoneNode();
                            }
                            break;

                        case Apply:

                            List<Node> arrayList = new LinkedList<>();

                            arrayList.add(eval(L.get(0), env));
                            arrayList.addAll(eval(L.get(1), env).getSubNodes());

                            retval = eval(Node.createExprNode(arrayList), env);
                            break;

                        case Quote:

                            retval = Node.createStringNode(eval(L.get(0), env).toString());
                            break;

//                        case "env":
//                            List a = new ArrayList();
//                            for (Map.Entry<String, Node> item : env.locals().entrySet()) {
//                                a.add(node.createListNode());
//                            }

                        case Let:
                            Env let_env = env.grow();
                            for (Node item : L.get(0).getSubNodes()) {
                                let_env.define(item.getSubNode(0).getValueString(), LibInterp.eval(item.getSubNode(1), env));
                            }
                            retval = eval(L.get(1), let_env);

                            break;

                        case Match:
                            Env match_env = env.grow();
                            List<Node> match_L = __eval_all(L.subList(0, 2), env);

                            if (__match(match_L.get(0), match_L.get(1), match_env)) {
                                retval = eval(L.get(2), match_env);
                            } else {
                                throw new InterpSystemError(ErrorNotMatch);
                            }

                            break;

                        case Import:

                            String path = eval(L.get(0), env).getValueString();
                            String prefix = L.size() == 2 ? L.get(1).getValueString() : "";
                            Path a;

                            if (path.startsWith("/") || path.startsWith("\\")) {
                                a = LibraryRoot.resolve(path.substring(1)).normalize();
                            } else {
                                a = ImportedPathStack.peek().resolve(path).normalize();
                            }

                            Env env_import;

                            ImportedPathStack.push(a.getParent());

                            try {

                                env_import = new Env();
                                aio(progn(new String(Files.readAllBytes(a))), env_import);

                            } catch (IOException e) {

                                throw new InterpSystemError(String.format(ErrorNotFoundFile, path));

                            } finally {

                                ImportedPathStack.pop();
                            }

                            env._import(env_import, prefix);

                            retval = Node.createNoneNode();

                            break;

                        case Export:

                            retval = eval(L.get(1), env);
                            env.export(L.get(0).getValueString(), retval);

                            break;

                        case Eval:

                            retval = aio(eval(L.get(0), env).getValueString(), env);
                            break;

                        case Type:

                            retval = Node.createTypeNode(eval(L.get(0), env).getType());
                            break;

                        case Exit:

                            throw new InterpSystemExit();

                        case Input:
                            Scanner inScanner = new Scanner(System.in);
                            retval = Node.createStringNode(inScanner.nextLine());
                            break;

                        case Output:

                            for (Node n : __eval_all(L, env)) {

                                System.out.print(n.getType().equals(StringType) ? n.getValueString() : n.toString());
                            }

                            break;

                        case And:

                            retval = Node.createBoolNode(__and(0, 1, L, env));
                            break;

                        case Or:

                            retval = Node.createBoolNode(__or(0, 1, L, env));
                            break;

                        case Not:

                            retval = eval(L.get(0), env).getValueBool() ? Node.createBoolNode(false) : Node.createBoolNode(true);
                            break;

                        case Assert:
                            if (!eval(L.get(0), env).getValueBool()) {

                                throw new InterpSystemAssert(L.get(0).toString());

                            } else {

                                retval = Node.createNoneNode();
                            }
                            break;

                        default:

                            List<Node> _L = __eval_all(L, env);

                            switch (op) {

                                case Get:
                                    retval = Operation.get(_L);
                                    break;

                                case Set:
                                    retval = Operation.set(_L);
                                    break;

                                case Insert:
                                    retval = Operation.insert(_L);
                                    break;

                                case Delete:
                                    retval = Operation.delete(_L);
                                    break;

                                case Copy:
                                    retval = Operation.copy(_L);
                                    break;

                                case Substr:
                                    retval = Operation.substr(_L);
                                    break;

                                case Concat:
                                    retval = Operation.concat(_L);
                                    break;

                                case Length:
                                    retval = Operation.length(_L);
                                    break;

                                case Encode:
                                    retval = Operation.encode(_L);
                                    break;

                                case Decode:
                                    retval = Operation.decode(_L);
                                    break;

                                case AddOp:
                                case Add:
                                    retval = Operation.add(_L);
                                    break;

                                case SubOp:
                                case Sub:
                                    retval = Operation.sub(_L);
                                    break;

                                case MulOp:
                                case Mul:
                                    retval = Operation.mul(_L);
                                    break;

                                case DivOp:
                                case Div:
                                    retval = Operation.div(_L);
                                    break;

                                case ModOp:
                                case Mod:
                                    retval = Operation.mod(_L);
                                    break;

                                case GtOp:
                                case Gt:
                                    retval = Operation.gt(_L);
                                    break;

                                case GeOp:
                                case Ge:
                                    retval = Operation.ge(_L);
                                    break;

                                case LtOp:
                                case Lt:
                                    retval = Operation.lt(_L);
                                    break;

                                case LeOp:
                                case Le:
                                    retval = Operation.le(_L);
                                    break;
                            }
                    }
                }
        }

        return retval;
    }
}


class Operation {

    private static int __index_to_offset(int i) {

        return i - 1;
    }

    public static Node get(List<Node> L) {

        Node _L = L.get(0);
        int i = L.get(1).getValueNumber().intValue();

        i = __index_to_offset(i);

        return _L.getSubNode(i);
    }

    public static Node set(List<Node> L) {

        Node _L = L.get(0);
        int i = L.get(1).getValueNumber().intValue();
        Node o = L.get(2);

        i = __index_to_offset(i);

        _L.replace(i, o);

        return _L;
    }

    public static Node insert(List<Node> L) {

        Node _L = L.get(0);
        int i = L.get(1).getValueNumber().intValue();
        Node o = L.get(2);

        i = __index_to_offset(i);

        if (_L == o) throw new InterpSystemError(ErrorCircleInsert);

        _L.insert(i, o);

        return _L;
    }

    public static Node delete(List<Node> L) {

        Node _L = L.get(0);
        int i = L.get(1).getValueNumber().intValue();

        i = __index_to_offset(i);

        _L.delete(i);

        return _L;
    }

    public static Node copy(List<Node> L) {

        return Node.createListNode(L.get(0).getSubNodes().stream().map(o -> o).collect(Collectors.toList()));
    }

    public static Node substr(List<Node> L) {

        String s = L.get(0).getValueString();
        int i = L.get(1).getValueNumber().intValue();
        int j = L.get(2).getValueNumber().intValue();

        i = __index_to_offset(i);
        j = __index_to_offset(j);

        return Node.createStringNode(s.substring(i, j + 1));
    }

    public static Node concat(List<Node> L) {

        StringBuilder builder = new StringBuilder();

        for (Node node : L) {

            builder.append(node.getValueString());
        }

        return Node.createStringNode(builder.toString());
    }

    public static Node length(List<Node> L) {

        if (L.get(0).getType().equals(StringType)) {

            return Node.createNumberNode(BigDecimal.valueOf(L.get(0).getValueString().length()));

        } else {
            // ListType
            return Node.createNumberNode(BigDecimal.valueOf(L.get(0).getSubNodesAmount()));
        }
    }

    public static Node encode(List<Node> L) {

        String s = L.get(0).getValueString();
        List<Node> _L = new LinkedList<>();

        for (char c : s.toCharArray()) {

            _L.add(Node.createNumberNode(BigDecimal.valueOf(c)));
        }

        return Node.createListNode(_L);
    }

    public static Node decode(List<Node> L) {

        Node _L = L.get(0);
        StringBuilder builder = new StringBuilder();

        for (Node node : _L.getSubNodes()) {

            builder.append(String.valueOf((char) node.getValueNumber().intValue()));
        }

        return Node.createStringNode(builder.toString());
    }

    public static Node add(List<Node> L) {

        BigDecimal num = BigDecimal.valueOf(0);

        for (Node node : L) {

            num = num.add(node.getValueNumber());
        }

        return Node.createNumberNode(num);
    }

    public static Node sub(List<Node> L) {

        BigDecimal num = L.get(0).getValueNumber();

        for (Node node : L.subList(1, L.size())) {

            num = num.subtract(node.getValueNumber());
        }

        return Node.createNumberNode(num);
    }

    public static Node mul(List<Node> L) {

        BigDecimal num = BigDecimal.valueOf(1);

        for (Node node : L) {

            num = num.multiply(node.getValueNumber());
        }

        return Node.createNumberNode(num);
    }

    public static Node div(List<Node> L) {

        try {
            BigDecimal num = L.get(0).getValueNumber();

            for (Node node : L.subList(1, L.size())) {

                num = num.divide(node.getValueNumber(), 3, BigDecimal.ROUND_CEILING);
            }

            return Node.createNumberNode(num);
        } catch (ArithmeticException e) {
            return Node.createNumberNode(BigDecimal.ZERO);
        }
    }

    public static Node mod(List<Node> L) {
        return Node.createNumberNode(L.get(0).getValueNumber().remainder(L.get(1).getValueNumber()));
    }

    public static Node gt(List<Node> L) {

        return Node.createBoolNode(__gt(0, 1, L));
    }

    private static boolean __gt(int i, int j, List<Node> L) {


        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return node_i.compareValueNumberTo(node_j) > 0 && __gt(j, j + 1, L);
        }
    }

    public static Node ge(List<Node> L) {

        return Node.createBoolNode(__ge(0, 1, L));
    }

    private static boolean __ge(int i, int j, List<Node> L) {


        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);
            return node_i.compareValueNumberTo(node_j) >= 0 && __ge(j, j + 1, L);
        }
    }

    public static Node lt(List<Node> L) {

        return Node.createBoolNode(__lt(0, 1, L));
    }

    private static boolean __lt(int i, int j, List<Node> L) {


        if (j == L.size()) {

            return true;

        } else {
            Node node_i = L.get(i);
            Node node_j = L.get(j);
            return node_i.compareValueNumberTo(node_j) < 0 && __lt(j, j + 1, L);
        }

    }

    public static Node le(List<Node> L) {

        return Node.createBoolNode(__le(0, 1, L));
    }

    private static boolean __le(int i, int j, List<Node> L) {


        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return node_i.compareValueNumberTo(node_j) <= 0 && __le(j, j + 1, L);
        }
    }
}

class Node {

    private String type;
    private Object value;
    private String expr;
    private List<Node> subNodes;
    private Env env = null;

    public Node(String type, Object value) {

        this.type = type;
        this.value = value;
        this.subNodes = new LinkedList<>();
    }

    public static Node createListNode(List<Node> subNodes) {

        Node node = new Node(ListType, ValueOfListType);
        node.subNodes = subNodes;

        return node;
    }

    public static Node createLambdaNode(List<Node> param, Env env) {

        Node node = new Node(LambdaType, ValueOfLambdaType);

        node.subNodes = param;
        node.env = env;

        return node;
    }

    public static Node createNoneNode() {

        return new Node(NoneType, ValueOfNoneType);
    }

    public static Node createBoolNode(boolean condition) {

        return condition ? new Node(BoolType, ValueOfBoolTypeTrue) : new Node(BoolType, ValueOfBoolTypeFalse);
    }

    public static Node createExprNode(List<Node> list) {

        Node node = new Node(ExprType, ValueOfExprType);
        node.subNodes = list;

        return node;
    }

    public static Node createTypeNode(String type) {

        return new Node(TypeType, type);
    }

    public static Node createNumberNode(BigDecimal number) {

        return new Node(NumberType, number);
    }

    public static Node createStringNode(String s) {

        return new Node(StringType, s);
    }

    public boolean eq(Node another) {

        if (!this.type.equals(another.type)) {

            return false;

        } else if (this.type.equals(NumberType)) {

            return compareValueNumberTo(another) == 0;

        } else if (this.type.equals(LambdaType)) {

            return this == another;

        } else if (this.type.equals(ExceptionType)) {

            return this.getValueString().equalsIgnoreCase(another.getValueString());

        } else if (this.type.equals(ListType)) {

            if (this.subNodes.size() != another.subNodes.size()) {

                return false;

            } else {

                for (int i = 0; i < this.subNodes.size(); i++) {

                    if (!this.subNodes.get(i).eq(another.subNodes.get(i))) return false;
                }

                return true;
            }

        } else {

            return this.value.equals(another.value);
        }
    }

    public Node addSubNode(Node node) {

        this.subNodes.add(node);

        return node;
    }

    public void replace(int i, Node o) {

        this.subNodes.set(i, o);
    }

    public void delete(int i) {

        this.subNodes.remove(i);
    }

    public String getType() {

        return this.type;
    }

    public String getValueString() {

        return (String) this.value;
    }

    public void insert(int i, Node o) {

        this.subNodes.add(i, o);
    }

    public BigDecimal getValueNumber() {

        return (BigDecimal) this.value;
    }

    public int compareValueNumberTo(Node another) {

        return ((BigDecimal) this.value).compareTo(((BigDecimal) another.value));
    }

    public boolean getValueBool() {

        return this.value.equals(ValueOfBoolTypeTrue);
    }

    public Node getSubNode(int i) {

        return this.subNodes.get(i);
    }

    public List<Node> getSubNodes() {

        return this.subNodes;
    }

    public int getSubNodesAmount() {

        return this.subNodes.size();
    }

    public Env getEnv() {

        return this.env;
    }

    public String toString() {

        if (this.expr != null && !this.type.equals(ListType)) {

            return this.expr;
        }

        switch (this.type) {

            case BoolType:
            case ExceptionType:
            case HandleType:
            case NoneType:
            case SymbolType:
            case TypeType:
            case NumberType:

                this.expr = this.value.toString();
                break;

            case StringType:

                this.expr = String.format("\"%s\"", ((String) this.value).replace("%", "%%").replace("\n", "%n").replace("\"", "%q"));
                break;

            case BlobType:

                this.expr = String.format("'%s'", this.value);
                break;

            case LambdaType:

                if (this.value.equals(ValueOfLambdaType)) {

                    this.expr = String.format(LambdaToStringFormat, this.subNodes.get(0).toString(), this.subNodes.get(1).toString());

                } else {

                    this.expr = (String) this.value;
                }
                break;

            case ListType:

                List<String> list_fragment = this.subNodes.stream().map(Node::toString).collect(Collectors.toList());
                this.expr = String.format("[%s]", String.join(" ", list_fragment));
                break;

            case ExprType:

                List<String> expr_fragment = this.subNodes.stream().map(Node::toString).collect(Collectors.toList());
                this.expr = String.format("(%s)", String.join(" ", expr_fragment));
                break;
        }

        return this.expr;
    }
}

class Env {

    private static final Env ENV = new Env();

    static {

        for (String lambda : new String[]{

                Define, Update, Import, Export,

                Cond, Is, Eq, EqOp, Lambda, Progn, If, Apply, Quote, Let, Match, Eval, Type, Exit,

                Input, Output,
//                "env",

                Assert,

                Length,

                Get, Set, Insert, Delete, Copy,

                Substr, Concat, Encode, Decode,

                And, Or, Not,

                Add, AddOp, Sub, SubOp, Mul, MulOp, Div, DivOp, Mod, ModOp,

                Gt, GtOp, Ge, GeOp, Lt, LtOp, Le, LeOp,
        }) {

            ENV.defines.put(lambda, new Node(LambdaType, lambda));
        }

//        ENV.defines.put("STDLIB", new Node(StringType, "C:\\Users\\NER0\\IdeaProjects\\interp\\lib\\"));

        ENV.parent = null;
        ENV.external = true;
        ENV.base = false;
    }

    private Env parent;
    private Map<String, Node> defines;
    private Map<String, Node> exports;
    private boolean external;
    private boolean base;

    public Env() {

        this.parent = ENV;
        this.defines = new HashMap<>();
        this.base = true;
    }

    public Env grow() {

        Env env = new Env();
        env.parent = this;
        env.base = false;

        return env;
    }

    public Node lookup(String name) {

        Node retval = this.defines.get(name);

        if (retval == null) {

            if (this.parent != null) {

                return this.parent.lookup(name);

            } else {

                throw new InterpSystemError(String.format(ErrorNotFoundSymbol, name));
            }

        } else {

            return retval;
        }
    }

    public void define(String name, Node value) {

        if (this.defines.containsKey(name)) throw new InterpSystemError(String.format(ErrorNameAlreadyExist, name));

        this.defines.put(name, value);
    }

    public void update(String name, Node value) {

        if (this.defines.containsKey(name)) {

            if (this.external) throw new InterpSystemError(String.format(ErrorExternalSymbol, name));
            else this.defines.replace(name, value);

        } else {

            if (this.parent != null) {

                this.parent.update(name, value);

            } else {

                throw new InterpSystemError(String.format(ErrorNotFoundSymbol, name));
            }
        }
    }

    public void _import(Env importedEnv, String prefix) {

        if (importedEnv.exports == null) return;

        if (!this.base) throw new InterpSystemError(ErrorCannotImportInSubEnv);

        Env env = new Env();

        env.parent = this.parent;
        env.external = true;

        for (Map.Entry<String, Node> item : importedEnv.exports.entrySet()) {

            env.define(prefix.isEmpty() ?
                            item.getKey() :
                            prefix + ImportNameSeparator + item.getKey(),
                    item.getValue());
        }

        this.parent = env;
    }

    public void export(String name, Node value) {

        if (!this.base) throw new InterpSystemError(ErrorCannotExportInSubEnv);

        if (this.exports == null) this.exports = new HashMap<>();

        if (this.exports.containsKey(name)) throw new InterpSystemError(String.format(ErrorNameAlreadyExist, name));

        this.exports.put(name, value);
    }

//    public Map<String, Node> locals(){
//        return this.defines;
//    }
}


class InterpSystemError extends RuntimeException {

    public InterpSystemError(String s) {

        super(String.format(ErrorPrefix, s));
    }
}

class InterpSystemAssert extends RuntimeException {

    public InterpSystemAssert(String s) {

        super(String.format(AssertionPrefix, s));
    }
}

class InterpSystemExit extends RuntimeException {

}

class Const {

    static final String NameOfPreviousValue = "_";

    static final String ConsolePrompt = "REPL";

    static final String SystemFinishedPrompt = "System finished with";

    static final String ErrorPrefix = "*** System Error: %s";
    static final String AssertionPrefix = "*** Assertion Fail: %s";
    static final String ErrorNotFoundSymbol = "Not found symbol '%s'";
    static final String ErrorNotFoundFile = "Not found file '%s'";
    static final String ErrorNotMatch = "Match not match";
    static final String ErrorSyntaxTooLittle = "Too little input";
    static final String ErrorSyntaxTooMuch = "Too many input";
    static final String ErrorSyntaxUndefined = "Undefined literal '%s'";
    static final String ErrorSyntaxIncorrectEscape = "Incorrect escape";
    static final String ErrorCircleInsert = "Can not insert into one's itself";
    static final String ErrorNameAlreadyExist = "Name already exist in current environment '%s'";
    static final String ErrorArgsAmount = "Arguments amount error, '%s' @ %s";
    static final String ErrorType = "Incorrect type";
    static final String ErrorExternalSymbol = "Forbidden to update external name '%s'";
    static final String ErrorCannotExportInSubEnv = "cannot export symbol in sub environment";
    static final String ErrorCannotImportInSubEnv = "cannot import module in sub environment";

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

    static final String ValueOfLambdaType = "closure";
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
    static final String Is = "is";
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
