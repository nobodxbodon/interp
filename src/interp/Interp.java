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

import static interp.ConstEnglish.*;

public class Interp {

    public static void main(String[] args) {

        switch (args.length) {

            case 0:

                Scanner inScanner = new Scanner(System.in);
                String in;
                Node out;

                Env env = new Env();
                env.define(NameOfPreviousValue, Node.createNoneNode());

                while (true) {

                    System.out.printf("[%s]<<< ", ConsolePrompt);

                    in = inScanner.nextLine();

                    if (in.isEmpty()) continue;

                    try {

                        out = LibInterp.aio(in, env);

                        env.update(NameOfPreviousValue, out);

                        System.out.printf("[%s]>>> ", ConsolePrompt);
                        System.out.println(out);

                    } catch (InterpSystemError e) {

                        System.out.println(e.getMessage());

                    } catch (InterpSystemExit interpSystemExit) {

                        System.exit(0);

                    } finally {

                        System.out.println();
                    }
                }

            case 1:

                String filePath = args[0];

                Node retval = Node.createNoneNode();

                try {

                    retval = LibInterp.aio(LibInterp.runMain(filePath), new Env());

                } catch (InterpSystemError e) {

                    System.out.println(e.getMessage());
                    e.printStackTrace();

                } catch (InterpSystemExit interpSystemExit) {

                    System.exit(0);
                }

                System.out.printf("\n%s %s", SystemFinishedPrompt, retval);

                break;

            default:

                System.out.println(Help);
        }
    }
}

class LibInterp {

    private static final Stack<Path> PathStack = new Stack<>();
    private static final Path InterpHome;

    static {
        PathStack.push(Paths.get(".").toAbsolutePath().normalize());
        InterpHome = Paths.get("C:\\Users\\NER0\\IdeaProjects\\interp\\lib");
    }

    public static Node aio(String code, Env env) throws InterpSystemExit, InterpSystemError {

        return eval(parse(lex(clean(code))), env);
    }

    public static String runMain(String filePath) {

        return String.format("(%s (%s \"%s\") (%s))", Progn, Import, filePath, Main);
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

        List<String> fragments = new ArrayList<>();

        String pattern = "\\(|\\)|\\[|\\]|\".*?\"|'.*?'|(?<=\\(|\\)|\\[|\\]|\\s|\"|'|^)\\S+?(?=\\(|\\)|\\[|\\]|\\s|\"|'|$)";
        Matcher matcher = Pattern.compile(pattern).matcher(code);

        while (matcher.find()) {

            fragments.add(code.substring(matcher.start(), matcher.end()));
        }

        return fragments;
    }

    private static String __escape(String s) throws InterpSystemError {

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

    private static Node __parse(Node parent, List<String> L) throws InterpSystemError {

        if (L.isEmpty()) throw new InterpSystemError(ErrorSyntaxTooLittle);

        String s = L.remove(0);
        Node node;

        if (s.equals("(")) {

            node = __parse(new Node(ExprType, ValueOfExprType), L);

        } else if (s.equals("[")) {

            node = __parse(new Node(ListType, ValueOfListType), L);

        } else if (s.equals(")") || s.equals("]")) {

            if (parent != null) {
                return parent;
            } else {
                throw new InterpSystemError(ErrorSyntaxTooLittle);
            }

        } else if (s.matches("^[+-]?\\d+(\\.\\d+([Ee]{1}[+-]?\\d+)?)?$")) {

            node = new Node(NumberType, new BigDecimal(s));

        } else if (s.startsWith("\"") && s.endsWith("\"")) {

            node = new Node(StringType, __escape(s.substring(1, s.length() - 1)));

        } else if (s.startsWith("'") && s.endsWith("'")) {

            node = new Node(BlobType, s.substring(1, s.length() - 1));

        } else if (s.startsWith("&")) {

            node = new Node(ExceptionType, s);

        } else if (s.startsWith("@")) {

            node = new Node(HandleType, s);

        } else if (s.startsWith("#")) {

            switch (s) {

                case ValueOfBoolTypeTrue:

                    node = new Node(BoolType, s);
                    break;

                case ValueOfBoolTypeFalse:

                    node = new Node(BoolType, s);
                    break;

                case ValueOfNoneType:

                    node = new Node(NoneType, s);
                    break;

                case BoundArgs:

                    node = new Node(SymbolType, s);
                    break;

                case BoundLambda:

                    node = new Node(SymbolType, s);
                    break;

                default:

                    throw new InterpSystemError(String.format(ErrorSyntaxUndefined, s));
            }

        } else if (s.startsWith(":")) {

            node = new Node(TypeType, s);

        } else {

            node = new Node(SymbolType, s);
        }


        if (parent == null) {

            if (!L.isEmpty()) throw new InterpSystemError(ErrorSyntaxTooMuch);

            return node;

        } else {

            parent.append(node);
            return __parse(parent, L);
        }
    }

    public static Node parse(List<String> fragments) throws InterpSystemError {

        return __parse(null, fragments);
    }

    private static boolean __eq(int i, int j, List<Node> L, Env env) throws InterpSystemExit, InterpSystemError {

        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return eval(node_i, env).eq(eval(node_j, env)) && __eq(j, j + 1, L, env);
        }
    }

    private static String __quote(Node node) {

        switch (node.getType()) {

            case StringType:

                String s = node.getValueString();
                List<Node> _L = new ArrayList<>();

                for (char c : s.toCharArray()) {

                    _L.add(Node.createNumberNode(BigDecimal.valueOf(c)));
                }

                return String.format(QuoteToStringFormat, Node.createListNode(_L));

            case LambdaType:

                if (node.getValueString().equals(ValueOfLambdaType)) {
                    return String.format(LambdaToStringFormat, node.getSubNode(0).toString(), __quote(node.getSubNode(1)));
                } else {

                    return node.getValueString();
                }

            case ExprType:

                return String.format("(%s)", String.join(" ", node.getSubNodes().stream().map(LibInterp::__quote).collect(Collectors.toList())));

            case ListType:

                return String.format("[%s]", String.join(" ", node.getSubNodes().stream().map(LibInterp::__quote).collect(Collectors.toList())));

            default:

                return node.toString();
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

    private static boolean __and(int i, int j, List<Node> L, Env env) throws InterpSystemExit, InterpSystemError {

        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return eval(node_i, env).getValueBool() && eval(node_j, env).getValueBool() && __and(j, j + 1, L, env);
        }
    }

    private static boolean __or(int i, int j, List<Node> L, Env env) throws InterpSystemExit, InterpSystemError {

        if (j == L.size()) {

            return true;

        } else {

            Node node_i = L.get(i);
            Node node_j = L.get(j);

            return (eval(node_i, env).getValueBool() || eval(node_j, env).getValueBool()) || __or(j, j + 1, L, env);
        }
    }

    private static List<Node> __eval_all(List<Node> L, Env env) throws InterpSystemExit, InterpSystemError {
        List<Node> _L = new ArrayList<>();

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
            case Trap:
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

    public static Node eval(Node node, Env env) throws InterpSystemExit, InterpSystemError {

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
            case TypeType:

                retval = node;
                break;

            case ListType:

                ArrayList<Node> list = new ArrayList<>();

                for (Node kid : node.getSubNodes()) {
                    list.add(eval(kid, env));
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

                            List<Node> arrayList = new ArrayList<>();

                            arrayList.add(eval(L.get(0), env));
                            arrayList.addAll(eval(L.get(1), env).getSubNodes());

                            retval = eval(Node.createExprNode(arrayList), env);
                            break;

                        case Quote:

                            retval = Node.createStringNode(__quote(eval(L.get(0), env)));
                            break;

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

                            String path = L.get(0).getValueString();
                            String prefix = L.size() == 2 ? L.get(1).getValueString() : "";
                            Path a;

                            if (path.startsWith("/") || path.startsWith("\\")){
                                a = InterpHome.resolve(path.substring(1)).normalize();
                            }else {
                                a = PathStack.peek().resolve(path).normalize();
                            }

                            Env env_import;

                            PathStack.push(a.getParent());

                            try {

                                env_import = new Env();
                                aio(progn(new String(Files.readAllBytes(a))), env_import);

                            } catch (IOException e) {

                                throw new InterpSystemError(String.format(ErrorNotFoundFile, path));

                            } finally {

                                PathStack.pop();
                            }

                            env._import(env_import, prefix);

                            retval = Node.createNoneNode();

                            break;

                        case Export:

                            retval = eval(L.get(1), env);
                            env.export(L.get(0).getValueString(), retval);

                            break;

                        case Eval:

                            retval = aio(__eval_all(L, env).get(0).getValueString(), env);
                            break;

                        case Type:

                            retval = Node.createTypeNode(eval(L.get(0), env).getType());
                            break;

                        case Exit:

                            throw new InterpSystemExit();

                        case Input:
                            Scanner inScanner = new Scanner(System.in);
                            retval = aio(inScanner.nextLine(), env);
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

                                throw new InterpSystemError(node.toString());

                            } else {

                                retval = Node.createNoneNode();
                            }
                            break;

                        case Trap:

                            throw new InterpSystemError(ErrorNotImplemented);

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
                                    retval = Operation.clone(_L);
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

    public static Node insert(List<Node> L) throws InterpSystemError {

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

    public static Node clone(List<Node> L) {

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
        List<Node> _L = new ArrayList<>();

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

    public static Node createListNode(List<Node> kids) {

        Node node = new Node(ListType, ValueOfListType);
        node.subNodes = kids;

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

            return ((BigDecimal) this.value).compareTo((BigDecimal) another.value) == 0;

        } else if (this.type.equals(LambdaType)) {

            return this == another;

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

    public Node append(Node node) {

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

                this.expr = String.format("\"%s\"", ((String) this.value).replace("%", "%%").replace("\n", "%n"));
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

                Cond, Eq, Lambda, Progn, If, Apply, Quote, Let, Match, Eval, Type, Exit,

                Input, Output,

                Assert, Trap,

                Length,

                Get, Set, Insert, Delete, Copy,

                Substr, Concat, Encode, Decode,

                And, Or, Not,

                Add, AddOp, Sub, SubOp, Mul, MulOp, Div, DivOp, Mod, ModOp,

                Gt, GtOp, Ge, GeOp, Lt, LtOp, Le, LeOp,
        }) {

            ENV.define.put(lambda, new Node(LambdaType, lambda));
        }

        ENV.parent = null;
    }

    private Env parent;
    private Map<String, Node> define;
    private Map<String, Node> export;

    public Env() {

        this.parent = ENV;
        this.define = new HashMap<>();
        this.export = new HashMap<>();
    }

    public Env grow() {

        Env aEnv = new Env();

        aEnv.parent = this;

        return aEnv;
    }

    public Node lookup(String name) throws InterpSystemError {

        Node retval = this.define.get(name);

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

        this.define.put(name, value);
    }

    public void update(String name, Node value) throws InterpSystemError {

        if (this.define.containsKey(name)) {

            this.define.replace(name, value);

        } else {

            if (this.parent != null) {

                this.parent.update(name, value);

            } else {

                throw new InterpSystemError(String.format(ErrorNotFoundSymbol, name));
            }
        }
    }

    public void _import(Env env, String prefix) {

        Env importEnv = new Env();

        importEnv.parent = this.parent;

        for (Map.Entry<String, Node> item : env.export.entrySet()) {

            importEnv.define(prefix.isEmpty() ?
                            item.getKey() :
                            prefix + ImportNameSeparator + item.getKey(),
                    item.getValue());
        }

        this.parent = importEnv;
    }

    public void export(String name, Node value) throws InterpSystemError {

        if (this.export.containsKey(name)) throw new InterpSystemError(String.format(ErrorNameAlreadyExport, name));

        this.export.put(name, value);
    }
}


class InterpSystemError extends Exception {

    public InterpSystemError(String s) {

        super(String.format(ErrorPrefix, s));
    }
}

class InterpSystemExit extends Exception {

}
