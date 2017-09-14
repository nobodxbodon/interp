# **L 编程语言讲演提纲**
# 语言说明

## Hello World
	
	;; main.l

	(import "/utils.l")
	
	(define display-hello-world (lambda [] (println "Hello World")))
	
	(export main (lambda [args] (display-hello-world)))


## 组合原理
类比于物质。分子由原子组成，无论多么复杂的分子，但是其必然是由有限个原子所构成的。分子和其他分子又可以结合成更大的分子，多种多样的分子最终构成了物质世界。

L语言有类似的比喻。在语言中，所谓原子是被称为元算子的算子，元算子相互组合，实现不同的功能。新的组合以同样的形式进一步构成其他的，更高级的组合，最终得到复杂的程序，并完成特定的功能。

L语言的元算子，按照其所代表的功能，可以大致分为以下几组：

    环境组： define, update, import, export
    结构组： cond, eq(=), lambda, progn, if, apply, quote, let, match, eval, 
			type, length, assert, exit
    交互组： input, output
    列表组： get, set, insert, delete, copy
    字串组： substr, concat, encode, decode
    逻辑组： and, or, not
    算术组： add(+), sub(-), mul(*), div(/), mod(%), gt(>), ge(>=), lt(<), le(<=)
    
总计 41 个。

**任何一个 L 语言所编写的程序，归根结底，都是由这 41 个元算子以及他们的复合形式构成的。**

元算子将来的扩增。

## 定义与更新

define，不因为定义对象的不同而有所区别，同一环境中不能重复定义。

update，只能更新之前 define 过的名字。

定义一个变量（名字，符号）：

	(define a-number 2)		; :number
	(define a-string "hello there")	; :string
	(define a-bool #true)	; :bool

定义一个函数（算子）：

	(define foobar (lambda [a b] (+ a b)))	; :lambda

形式一致，名字不标记类型，值有类型。

	(update a-number 3)
	(update a-number (add 1 a-number))

没有语句与表达式的区分，所有的都是表达式，一致的调用的形式：

	(某某算子 参数1 参数2 参数3 ...)

所有的表达式都返回一个值，没什么可返回的，则返回一个 #none。

将表达式组合起来，形成复杂的程序。

## 流程控制

顺序： progn，返回最后一个元素的求值结果。

选择： cond， if，按条件返回结果。

循环： 递归

三种流程。

通常的编程语言，预设了顺序的流程。类似：

	int a = 1;
	int b = 2;
	int c = a + b;

L语言中，顺序也是一个控制流程：

	(progn
		(define a 1)
		(define b 2)
		(define c (+ a b))
	)

此例中，(progn) 返回 3。解释之。

执行文件时，隐含的顺序语义。

一般的语言中，if是不返回值的语句，有返回值的 ?: 三元运算符。

	if (i == 0) { return 1; }
	else { return 2; }

	return (i == 0) ? 1 : 2;

L语言中，没有语句，故而：

	(if (eq i 0) 1 2)   => will return 1 or 2;
	(if (eq i 0) (progn
		...
	) (progn
		...
	))

另一种形式，适合多分枝的结构：

	(cond [(条件1) (结果1)] [(条件2) (结果2)] ...)

	(cond
		[(eq i 0) 1]
		[#true    2]
	)

可以类比于 switch，只是更高级，任意的判断条件。

循环就是函数调用自身，循环改变的量通过参数机制传入。

	(define factorial  (lambda [n] 
		(if (eq n 0) 		; handle 0!
			1 
			(* n (#lambda (- n 1)))
	)))

## 函数的定意

形式

	(lambda [参数列表] <操作的组合>)

参数可以多个或零个。

前者，输入参数数目必须等同于定义数目。后者，表示零个参数，不限参数，任意参数的概念。

二者在逻辑上是自洽的。

预定义#lambda符号，在函数内，表示其自身，实现匿名函数的递归。如上例：

也可以使用下面的形式定意匿名函数的递归：

	((lambda [op] (op op)) (lambda [op] (lambda [...] <(op op)>)))

预定义#args符号，在函数内，表示所有输入函数的值的列表：

	((lambda [] #args) 1 2 3)  =>  [1 2 3]

## 类型与表示
L语言设计了 12 种类型，每一个元素都有其类型，有其特定形式的字面量表达方式：
	
	:expr 		-> 	() (...) (add 1 2)
	:symbol 	-> 	a b c d

	:list 		-> 	[] [1 2 3] [23 (add 1 2)]

	:number 	-> 	1 2 3 42 -23.32e+3
	:string 	-> 	"" "abc"
	:exception 	-> 	&NOT-FOUND, &EOF
	:none 		-> 	#none
	:bool 		-> 	#true #false
	:lambda 	-> 	add sub (lambda [a b] (add a b))
	
	:blob 		-> 	'133FFFBADD'
	:handle 	-> 	@854 @FDC20FF

	:type 		-> 	:expr :symbol :list 

特殊的字面量，以 # 开头，包括 5 个：
	
	#true
	#false 
	#none
	#args
	#lambda

通过 (type) 获得某元素的类型：

	(type 1) => :number

	(define a 2)
	(type a) => :number 	  ; not :symbol
	(type (+ a 1)) => :number ; not :expr

	(type :number) => :type
	(type :type)   => :type

## 转义与求值

改良的转义字符，以 % 代替 \ ，避免斜杠地狱，目前仅仅支持：
	
	"%n"	换行
	"%q"	引号
	"%%"	转义符本身

使用 (quote) 序列化任意元素，得到字符串；使用 (eval) 对字符串表示的元素求值，得到结果。示例：

	$ java -jar interp.jar
	[REPL]<<< (define foo (lambda [] (progn "hehehehe" (lambda [] (progn "hahaha")))))
	[REPL]>>> (lambda [] (progn "hehehehe" (lambda [] (progn "hahaha"))))
	
	[REPL]<<< (define bar (quote foo))
	[REPL]>>> "(lambda [] (progn %qhehehehe%q (lambda [] (progn %qhahaha%q))))"
	
	[REPL]<<< (type bar)
	[REPL]>>> :string
	
	[REPL]<<< (eval bar)
	[REPL]>>> (lambda [] (progn "hehehehe" (lambda [] (progn "hahaha"))))
	
	[REPL]<<< ((_))
	[REPL]>>> "hahaha"
	
	[REPL]<<< ((foo))
	[REPL]>>> "hahaha"

## 异常处理机制

加入异常类型，采用异常类型的返回值，而非抛出异常，两种执行流程转化为一种。

	(define foo (bar))
	(cond
		[(eq foo &NOT-FOUND) ...]
		...
		[#true (progn
			...
	)])

用法是，判断输出结果的类型，如果是正常的值则处理，如果等价于特定异常则执行异常分枝。

## 动态调用
可以将参数打包，变形，传递，解包等等。形式

	(apply op [a b c ...])

等价于：

	(op a b c ...)

如：

	[REPL]<<< (map (lambda [args] (apply + args)) [[58 21 45] [21 25]])
	[REPL]>>> [124 46]

## 短路求值的逻辑判断

	and or not

## let 与 match，子环境与模式匹配

let定意的名字只在let内有效，临时的，局部的，“在容器里”， (let [[<键值对1>] [<键值对2>] ...] <使用键值对的表达式>)。

	[REPL]<<< (let [[i 21] [j 23]] (+ i j))
	[REPL]>>> 44
	
	[REPL]<<< i
	*** System Error: Not found symbol 'i'
	
	[REPL]<<< j
	*** System Error: Not found symbol 'j'

方便定意名字的绑定。

模式匹配 (match <欲匹配的结构> <匹配模式> <使用模式中定意的名字>)
	
	[REPL]<<< (match [1 2 3 4 5] ["a" "" "b" "" "c"] [a b c])
	[REPL]>>> [1 3 5]
	
	[REPL]<<< (match [1 2 [3 4] 5] ["a" "" ["b" ""] "c"] [a b c])
	[REPL]>>> [1 3 5]

如此，处理复杂的数据结果，不用一个个地将其取出，直接匹配之则可。

## 闭包

	[REPL]<<< (define foo (lambda [n] (lambda [i] (update n (+ n i)))))
	[REPL]>>> (lambda [n] (lambda [i] (update n (+ n i))))
	
	[REPL]<<< (define foo5 (foo 5))
	[REPL]>>> (lambda [i] (update n (+ n i)))
	
	[REPL]<<< (foo5 10)
	[REPL]>>> 15
	
	[REPL]<<< (foo5 100)
	[REPL]>>> 115
	
	[REPL]<<< (define foo3 (foo 3))
	[REPL]>>> (lambda [i] (update n (+ n i)))
	
	[REPL]<<< (foo3 27)
	[REPL]>>> 30

## 自省
	
以 type 算子获得任意元素的类型，自省：

	(type #true)  =>  :bool
	(type 13)     =>  :number
	(type :bool)  =>  :type

以 length 算子获得元素的长度：

	(length []) 	 => 0
	(length [1 2 3]) => 3
	(length "Hello") => 5

类型比较

	(eq :number :number)
	(eq (type 23) :number)

在函数内，以自省验证输入，而不是使用类型标记：

	(lambda [name] (progn
		(assert (and (eq (type name) :string) (eq (length name) 10)))
	))

## 重载与多态

同样一个算子，判断类型，转发调用。

	[REPL]<<< +
	[REPL]>>> +
	
	[REPL]<<< (define + (let [[+ +]] (lambda [a b] (cond [(eq (type a) (type b) :number) (+ a b)] [(eq (type a) (type b) :string) (concat a b)]))))
	[REPL]>>> (lambda [a b] (cond [(eq (type a) (type b) :number) (+ a b)] [(eq (type a) (type b) :string) (concat a b)]))
	
	[REPL]<<< +
	[REPL]>>> (lambda [a b] (cond [(eq (type a) (type b) :number) (+ a b)] [(eq (type a) (type b) :string) (concat a b)]))
	
	[REPL]<<< (+ 1 1)
	[REPL]>>> 2
	
	[REPL]<<< (+ "Hello" "World")
	[REPL]>>> "HelloWorld"

多态的例子，如：
	
	(define create-with-number ...)
	(define create-with-string ...)

	(define create (lambda [...] (progn
		...
		(if (...) (create-with-number ...))
		(if (...) (create-with-string ...))
		...
	)))

## 模块化支持

L语言通过 import 和 export 算子支持模块化编程。

import 的语法

	(import <模块路径> <别名>)
	
	(import "/utils.l")
	(import "utils.l")

模块路径是一个字符串，指定模块的位置，有两种形式，即相对引用和绝对引用。

相对引用，是指基于本模块本身的位置来引用。

绝对引用，是指基于系统的库目录的位置来引用。

程序需要特定版本的第三方模块，则可以在自己的目录中包含之，并使用相对引用方法使用之，而不是位于系统库目录下的版本。

别名是可选的，目的在于修饰所有导入的名字，以避免诸导入模块的名字覆盖问题。

比如，在utils.l模块导出了名字 create-something，如果：

	(import "/utils.l")
	(create-something ...)

对比于：

	(import "/utils.l" utils)
	(utils-create-something ...)

中划线是L语言的命名分割约定。

不像Java试图在全局解决包名字重复的问题，L语言只需要在模块导入时，如有冲突，临时改名而已。

关键字：根环境。主环境。导入环境。当前环境。两组名字。环境导入。两个名字，一个函数。环境生长。在生长的环境中导入。多个生长的环境可以有有不同的依赖。

结合附录图标，使用 hello world 的例子，解释依赖关系图，介绍名字定意，查找，更新的过程。

顺序介绍 hello world的执行过程。

程序构成两种视角，模块化的，函数化的。

## 模仿面向对象的编程，针对接口的编程
面向对象编程的三类操作，创建对象的方法，修改对象的方法，由对象生成其他对象的方法。

news对象，七个操作。

一个Java的例子

	// News.java
	
	public class News {
	    
	    private Integer id;
	    private String  title;
	    private String  content;
	    
	    News(Integer id, String title, String content){
	    
	        this.id = id;
	        this.title = title;
	        this.content = content;
	    }
	    
	    public void setID(Integer id) { this.id = id; }
	    public void setTitle(String title) { this.title = title; }
	    public void setContent(String content) { this.content = content; }
	    
	    public Integer getID() { return this.id; }
	    public String getTitle() { return this.title; }
	    public String getContent() { return this.content }
	}

L语言对应例子：

	;; News.l
	
	(define m-id 1)
	(define m-title 2)
	(define m-content 3)
	
	(export news-create (lambda [id title content] [id title content]))
	
	(export news-set-id (lambda [news id] (set news m-id id)))
	(export news-set-title (lambda [news title] (set news m-title title)))
	(export news-set-content (lambda [news content] (set news m-content content)))
	
	(export news-get-id (lambda [news] (get news m-id)))
	(export news-get-title (lambda [news] (get news m-title)))
	(export news-get-content (lambda [news] (get news m-content)))

执行实验：

	NER0@NER0-PC C:\Users\NER0\IdeaProjects\interp\out\artifacts\interp_jar
	$ java -jar interp.jar
	[REPL]<<< (import "../../../example/news.l")
	[REPL]>>> #none
	
	[REPL]<<< news-create
	[REPL]>>> (lambda [id title content] [id title content])
	
	[REPL]<<< m-id
	*** System Error: Not found symbol 'm-id'
	
	[REPL]<<< (define news (news-create 1 "天气不错" "今天天气不错哦"))
	[REPL]>>> [1 "天气不错" "今天天气不错哦"]
	
	[REPL]<<< (news-get-id news)
	[REPL]>>> 1
	
	[REPL]<<< (news-get-title news)
	[REPL]>>> "天气不错"
	
	[REPL]<<< (news-get-content news)
	[REPL]>>> "今天天气不错哦"
	
	[REPL]<<< (news-set-id news 22)
	[REPL]>>> [22 "天气不错" "今天天气不错哦"]
	
	[REPL]<<< (news-set-title news "Good Weather")
	[REPL]>>> [22 "Good Weather" "今天天气不错哦"]
	
	[REPL]<<< (news-set-content news "This is a good weather, haha.")
	[REPL]>>> [22 "Good Weather" "This is a good weather, haha."]
	
	[REPL]<<< news
	[REPL]>>> [22 "Good Weather" "This is a good weather, haha."]
	

## 基于关系的数据表示，直观的序列化

数据的构成也遵循组合原理。**所有数据，都是由原子，以及包含原子的列表组合而成的。**

原理：以结构定义类型，定意若干项之间的“关系”，但并不约束这种关系具体是什么。

序列，数组，容器：

	[11 22 33]
	[1 2 (add 1 2)]
	[11 "abcd" #true #false]

简单的键值对格式：

	[["name" "Peter"] ["gender" "male"] ["age" 29]]

对象的模拟，类型说明：
	
	["Profile" [["name" "Peter"] ["gender" "male"] ["age" 29]]]

HashTable：

	[
	    [1 [[键值对] [键值对] [键值对] ...]]
	    [2 [[键值对] [键值对] [键值对] ...]]
	    [3 [[键值对] [键值对] [键值对] ...]]
	    [4 [[键值对] [键值对] [键值对] ...]]
	    ...
	]


模仿常见的数据结构形式，对于 JSON :

	{
	     "firstName": "John",
	     "lastName": "Smith",
	     "gender": "male",
	     "age": 25,
	     "address": 
	     {
	         "streetAddress": "21 2nd Street",
	         "city": "New York",
	         "state": "NY",
	         "postalCode": "10021"
	     },
	     "phoneNumber": 
	     [
	         {
	           "type": "home",
	           "number": "212 555-1234"
	         },
	         {
	           "type": "fax",
	           "number": "646 555-4567"
	         }
	     ]
	}

对应的：
	
	[
	    ["firstName" "John"]
	    ["lastName"  "Smith"]
	    ["gender"    "male"]
	    ["age"       "25"]
	    ["address" [
	        ["streetAddress" "21 2nd Street"]
	        ["city"          "New York"]
	        ["state"         "NY"]
	        ["postalCode"    "10021"]
	    ]]
	    ["phoneNumber" [
	        [["type" "home"] ["number" "212 555-1234"]]
	        [["type" "fax"]  ["number" "646 555-4567"]]
	    ]]
	]

对于 xml ：

	<?xml version="1.0"?>
	<小纸条>
	    <收件人>大元</收件人>
	    <發件人>小張</發件人>
	    <主題>問候</主題>
	    <具體內容>早啊，飯吃了沒？ </具體內容>
	</小纸条>

对应的：

	[
	    ["xml" [["version" "1.0"]]]
	    ["小纸条" [] [
	        ["收件人" [] ["大元"]]
	        ["发件人" [] ["小张"]]
	        ["主题"  [] ["问候"]]
	        ["具体内容" [] ["早啊，饭吃了没？"]]
	    ]]
	]

数据表示和代码表示在形式上的完美一致，美感：

	(add 1 2 3 4)
	[add 1 2 3 4]

包含L语言代码的字符串也可以作为通用的协议表示方法，统一了语言和协议的概念。类比人用中文思考，用中文交谈。程序用 L 语言编程，并交换 L 语言代码以通信：

http请求类比

	GET / HTTP/1.1
	Host: www.google.com
	
	(get "/" "HTTP/1.1" [["host" "www.google.com"]])

http响应类比
	
	HTTP/1.1 200 OK
	Content-Length: 3059
	Server: GWS/2.0
	Date: Sat, 11 Jan 2003 02:44:04 GMT
	Content-Type: text/html
	Cache-control: private
	Set-Cookie: PREF=ID=73d4aef52e57bae9:TM=1042253044:LM=1042253044:S=SMCc_HRPCQiqy
	X9j; expires=Sun, 17-Jan-2038 19:14:07 GMT; path=/; domain=.google.com
	Connection: keep-alive

	["OK" 200 "HTTP/1.1" [
		["Content-Length" 3059]
		["Server" "GWS/2.0"]
		...
	]]

本地调用

	(add 1 2 3)

远程调用，发送代码字符串

	"(add 1 2 3)"

操作和参数都给出了，不用其他形式的表示方法。
	
远程定义了操作 foo 和 bar，但是需要判断远程的某值是不是等于 0，协议的**组合性**。

	"(if (eq n 0) (foo) (bar))"


## 中文编程

L语言是一个**完全中文**的编程语言。通过汉化每一个语法单元，根据组合原理，就可以得到一个完全汉化的编程语言，类似的也可以做出使用任意自然语言的编程语言。

	(define fibonacci (lambda [n] 
	    ((lambda [a b i] (cond
	        [(eq i n) []]
	        [#true ((lambda [atom L] (insert L 1 atom)) 
	            a 
	            (#lambda b (add a b) (add i 1))
	        )]
	    )) 0 1 0)
	))
	
	(export main (lambda [args] (progn
	    (output "Evaluate Fibonacci List%n")
	    (fibonacci 10)
	)))
	
	==============================================
	
	Evaluate Fibonacci List
	
	System finished with [0 1 1 2 3 5 8 13 21 34]

对比中文版：

	(定意 斐波纳兹数列 (算法 [上限] 
	    ((算法 [前一 前二 计数] (选择
	        [(等于 计数 上限) []]
	        [#真 ((算法 [元素 列表] (插入 列表 1 元素)) 
	            前一 
	            (#算法 前二 (加 前一 前二) (加 计数 1))
	        )]
	    )) 0 1 0)
	))
	
	(导出 入口 (算法 [入口参数] (顺序
	    (输出 "求取斐波纳兹数列%n")
	    (斐波纳兹数列 10)
	)))
	
	==============================================
	
	求取斐波纳兹数列
	
	系统中止并输出 [0 1 1 2 3 5 8 13 21 34]

交互使用：

	NER0@NER0-PC C:\Users\NER0\IdeaProjects\interp\out\artifacts\interp_jar
	$ java -jar interp.jar
	[解释器]<<< (定意 数列 [1 2 3 4 5])
	[解释器]>>> [1 2 3 4 5]
	
	[解释器]<<< (你好)
	*** 系统错误: 未找到符号 你好
	
	[解释器]<<< (施用 加 数列)
	[解释器]>>> 15
	
	[解释器]<<< (乘 *前者* 25)
	[解释器]>>> 375
	
	[解释器]<<< ((算法 [列表] (选择 [(等于 列表 []) #空] [#真 (顺序 (输出 ((取 列表 1) 10 4) "%n") (#算法 (删除 (复制 列表) 1)))])) [加 减 乘 除])
	14
	6
	40
	2.500
	[解释器]>>> #空
	
	[解释器]<<< (退出)

# 标准库示例
标准库代码见附件

## LISP 三算子

	(export car  (define car  (lambda [L]   (get L 1))))
	(export cdr  (define cdr  (lambda [L]   (delete (copy L) 1))))
	(export cons (define cons (lambda [o L] (insert (copy L) 1 o))))

## 结构操作
	
	filter, map, reduce, unique, reverse, enum, slice, flat, zip, exclude

例子：

	[REPL]<<< (define L (range 1 10))
	[REPL]>>> [1 2 3 4 5 6 7 8 9 10]
	
	[REPL]<<< (filter (lambda [n] (> n 5)) L)
	[REPL]>>> [6 7 8 9 10]
	
	[REPL]<<< (map inc L)
	[REPL]>>> [2 3 4 5 6 7 8 9 10 11]
	
	[REPL]<<< (reduce (lambda [a b] (+ a b)) L)
	[REPL]>>> 55

	[REPL]<<< L
	[REPL]>>> [1 2 3 4 5 6 7 8 9 10]
	
	[REPL]<<< (insert L 1 (copy L))
	[REPL]>>> [[1 2 3 4 5 6 7 8 9 10] 1 2 3 4 5 6 7 8 9 10]
	
	[REPL]<<< (flat L)
	[REPL]>>> [1 2 3 4 5 6 7 8 9 10 1 2 3 4 5 6 7 8 9 10]
	
	[REPL]<<< (unique _)
	[REPL]>>> [1 2 3 4 5 6 7 8 9 10]
	
	[REPL]<<< (reverse _)
	[REPL]>>> [10 9 8 7 6 5 4 3 2 1]
	
	[REPL]<<< (enum _)
	[REPL]>>> [[1 10] [2 9] [3 8] [4 7] [5 6] [6 5] [7 4] [8 3] [9 2] [10 1]]
	
	[REPL]<<< (slice _ 1 5)
	[REPL]>>> [[1 10] [2 9] [3 8] [4 7] [5 6]]
	
	[REPL]<<< (flat _)
	[REPL]>>> [1 10 2 9 3 8 4 7 5 6]
	
	[REPL]<<< (exclude _ 10)
	[REPL]>>> [1 2 9 3 8 4 7 5 6]
	
	[REPL]<<< (merge _ _ _)
	[REPL]>>> [1 2 9 3 8 4 7 5 6 1 2 9 3 8 4 7 5 6 1 2 9 3 8 4 7 5 6]
	
	[REPL]<<< (unique _)
	[REPL]>>> [1 2 9 3 8 4 7 5 6]
	
	[REPL]<<< (zip _ _ _)
	[REPL]>>> [[1 1 1] [2 2 2] [9 9 9] [3 3 3] [8 8 8] [4 4 4] [7 7 7] [5 5 5] [6 6 6]]
	
	[REPL]<<< (unique (flat _))
	[REPL]>>> [1 2 9 3 8 4 7 5 6]
	
	[REPL]<<< (qsort _)
	[REPL]>>> [1 2 3 4 5 6 7 8 9]

	[REPL]<<< (append _ 10)
	[REPL]>>> [1 2 3 4 5 6 7 8 9 10]

## 管道

	～

	[REPL]<<< (~ 1 [inc inc inc])
	[REPL]>>> 4

	[REPL]<<< (~ 1 [inc exp2 exp3])
	[REPL]>>> 64
	
	[REPL]<<< (exp3 (exp2 (inc 1)))
	[REPL]>>> 64

## 生成器

	take

例子：

	NER0@NER0-PC C:\Users\NER0\IdeaProjects\interp\out\artifacts\interp_jar
	$ java -jar interp.jar
	[REPL]<<< (import "/utils.l")
	[REPL]>>> #none
	
	[REPL]<<< (define range (let [[start 0] [end 27] [step 1]] (lambda [] (if (= start end) &END (update start (+ start step))))))
	[REPL]>>> (lambda [] (if (= start end) &END (update start (+ start step))))
	
	[REPL]<<< (take 10 range)
	[REPL]>>> [1 2 3 4 5 6 7 8 9 10]
	
	[REPL]<<< (take 10 range)
	[REPL]>>> [11 12 13 14 15 16 17 18 19 20]
	
	[REPL]<<< (take 10 range)
	[REPL]>>> [21 22 23 24 25 26 27]
	
	[REPL]<<< (take 10 range)
	[REPL]>>> []


## 数据格式验证

以格式代表数据类型

	validate

实验：

	NER0@NER0-PC C:\Users\NER0\IdeaProjects\interp\out\artifacts\interp_jar
	$ java -jar interp.jar
	[REPL]<<< (import "/utils.l")
	[REPL]>>> #none
	
	[REPL]<<< validate
	[REPL]>>> (lambda [L Format] (progn (define atomic [:number :string :bool :none :exception :blob :handle]) ((lambda [L Format] (cond [(in (type Format) atomic) (eq Format L)] [(type-eq Format :type) (eq (type L) Format)] [(type-eq Format :lambda) (Format L)] [(eq (type L) (type Format) :list) (cond [(eq (length L) (length Format) 0) #true] [(eq (length L) (length Format)) (and (#lambda (car L) (car Format)) (#lambda (cdr L) (cdr Format)))] [#true #false])] [#true #false])) L Format)))
	
	[REPL]<<< (validate [1 2 3] [:number :number :number])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 2 3] [:number :number 3])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 2 3] [:number :number (lambda [n] (eq n 3))])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 2 3] [:number :number (lambda [n] (eq n 2))])
	[REPL]>>> #false
	
	[REPL]<<< (validate [1 2 3] (lambda [L] (eq (apply + L) 6)))
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 2 3] (lambda [L] (eq (apply + L) 5)))
	[REPL]>>> #false
	
	[REPL]<<< (validate [1 [2] 3] [1 [2] 3])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 [2] 3] [1 2 3])
	[REPL]>>> #false
	
	[REPL]<<< (validate [1 [2] 3] [1 :list 3])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 [2] 3] [])
	[REPL]>>> #false
	
	[REPL]<<< (validate [1 [2 23] 3 [2]] [:number :list 3 (lambda [L] (and (type-eq L :list) (length-eq L 1) (type-eq (car L) :number)))])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 [2 23] 3 [10]] [:number :list 3 (lambda [L] (and (type-eq L :list) (length-eq L 1) (type-eq (car L) :number)))])
	[REPL]>>> #true
	
	[REPL]<<< (validate [1 [2 23] 3 ["10"]] [:number :list 3 (lambda [L] (and (type-eq L :list) (length-eq L 1) (type-eq (car L) :number)))])
	[REPL]>>> #false

## 操作字典结构的数据
	
	dict-get dict-set dict-insert dict-delete dict-names dict-values

例子

	NER0@NER0-PC C:\Users\NER0\IdeaProjects\interp\out\artifacts\interp_jar
	$ java -jar interp.jar
	[REPL]<<< (import "/utils.l")
	[REPL]>>> #none
	
	[REPL]<<< (define L [     ["firstName" "John"]     ["lastName"  "Smith"]     ["gender"    "male"]
	 ["age"       "25"]     ["address" [         ["streetAddress" "21 2nd Street"]         ["city"
	   "New York"]         ["state"         "NY"]         ["postalCode"    "10021"]     ]]     ["phoneNumber" [         [["type" "home"] ["number" "212 555-1234"]]         [["type" "fax"]  ["number" "646 555-4567"]]     ]] ])
	[REPL]>>> [["firstName" "John"] ["lastName" "Smith"] ["gender" "male"] ["age" "25"] ["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" "10021"]]] ["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]]]
	
	[REPL]<<< (each println L)
	["firstName" "John"]
	["lastName" "Smith"]
	["gender" "male"]
	["age" "25"]
	["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" "10021"]]]
	["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]]
	[REPL]>>> #none
	
	[REPL]<<< (dict-get L "age")
	[REPL]>>> "25"
	
	[REPL]<<< (dict-set L "age" 30)
	[REPL]>>> ["age" 30]
	
	[REPL]<<< L
	[REPL]>>> [["firstName" "John"] ["lastName" "Smith"] ["gender" "male"] ["age" 30] ["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" "10021"]]] ["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]]]
	
	[REPL]<<< (dict-set (dict-get L "address") "postalCode" 12345)
	[REPL]>>> ["postalCode" 12345]
	
	[REPL]<<< L
	[REPL]>>> [["firstName" "John"] ["lastName" "Smith"] ["gender" "male"] ["age" 30] ["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" 12345]]] ["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]]]
	
	[REPL]<<< (dict-insert L "chinese-name" "小明")
	[REPL]>>> [["firstName" "John"] ["lastName" "Smith"] ["gender" "male"] ["age" 30] ["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" 12345]]] ["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]] ["chinese-name" "小明"]]
	
	[REPL]<<< (dict-delete L "firstName")
	[REPL]>>> [["lastName" "Smith"] ["gender" "male"] ["age" 30] ["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" 12345]]] ["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]] ["chinese-name" "小明"]]
	
	[REPL]<<< (dict-delete L "lastName")
	[REPL]>>> [["gender" "male"] ["age" 30] ["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" 12345]]] ["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]] ["chinese-name" "小明"]]
	
	[REPL]<<< (each println L)
	["gender" "male"]
	["age" 30]
	["address" [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" 12345]]]
	["phoneNumber" [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]]]
	["chinese-name" "小明"]
	[REPL]>>> #none
	
	[REPL]<<< (dict-names L)
	[REPL]>>> ["gender" "age" "address" "phoneNumber" "chinese-name"]
	
	[REPL]<<< (dict-values L)
	[REPL]>>> ["male" 30 [["streetAddress" "21 2nd Street"] ["city" "New York"] ["state" "NY"] ["postalCode" 12345]] [[["type" "home"] ["number" "212 555-1234"]] [["type" "fax"] ["number" "646 555-4567"]]] "小明"]
	
## 快速排序算法
层层定义，得到最终程序，组合关系。输入量化函数，对任意类型排序，代码组合的灵活性。

	(export quick-sort (define quick-sort (lambda [L Fn] (cond
	    [(<= (length L) 1) L]
	    [#true (merge
	        (#lambda (filter (lambda [i] (< (Fn i) (Fn (car L)))) (cdr L)) Fn)
	        [(car L)]
	        (#lambda (filter (lambda [i] (>= (Fn i) (Fn (car L)))) (cdr L)) Fn)
	    )]
	))))

	[REPL]<<< L
	[REPL]>>> [[1 10 72] [2 9 101] [3 8 108] [4 7 108] [5 6 111] [6 5 87] [7 4 111] [8 3 114] [9 2 108] [10 1 100]]
	
	[REPL]<<< (each println L)
	[1 10 72]
	[2 9 101]
	[3 8 108]
	[4 7 108]
	[5 6 111]
	[6 5 87]
	[7 4 111]
	[8 3 114]
	[9 2 108]
	[10 1 100]
	[REPL]>>> #none

依次以三项为比较值进行比较：
	
	[REPL]<<< (each println (quick-sort L 1st))
	[1 10 72]
	[2 9 101]
	[3 8 108]
	[4 7 108]
	[5 6 111]
	[6 5 87]
	[7 4 111]
	[8 3 114]
	[9 2 108]
	[10 1 100]
	[REPL]>>> #none
	
	[REPL]<<< (each println (quick-sort L 2nd))
	[10 1 100]
	[9 2 108]
	[8 3 114]
	[7 4 111]
	[6 5 87]
	[5 6 111]
	[4 7 108]
	[3 8 108]
	[2 9 101]
	[1 10 72]
	[REPL]>>> #none
	
	[REPL]<<< (each println (quick-sort L 3rd))
	[1 10 72]
	[6 5 87]
	[10 1 100]
	[2 9 101]
	[3 8 108]
	[4 7 108]
	[9 2 108]
	[5 6 111]
	[7 4 111]
	[8 3 114]
	[REPL]>>> #none

以三项的和为比较值：

	[REPL]<<< (each println (quick-sort L (lambda [L] (apply + L))))
	[1 10 72]
	[6 5 87]
	[10 1 100]
	[2 9 101]
	[3 8 108]
	[4 7 108]
	[9 2 108]
	[5 6 111]
	[7 4 111]
	[8 3 114]
	[REPL]>>> #none
		
# 代码实现

结构：

	Interp
	    main
	    
	LibInterp
	    ParseRollback
	    
	    aio
	    runMain
	    progn
	    clean
	    lex
	        __escape
	        __parse
	        __identify
	    parse
	        __eq
	        __match
	        __and
	        __or
	        __eval_all
	        __check_args_amount
	    eval
	    
	Operation
	        __index_to_offset
	    get
	    set
	    insert
	    delete
	    clone
	    substr
	    concat
	    length
	    encode
	    decode
	    add
	    sub
	    mul
	    div
	    mod
	    gt
	        __gt
	    ge
	        __ge
	    lt
	        __lt
	    le
	        __le
	Node
	    Node
	    
	    createListNode
	    createLambdaNode
	    createNoneNode
	    createBoolNode
	    createExprNode
	    createTypeNode
	    createNumberNode
	    createStringNode
	    
	    eq
	    addSubNode
	    replace
	    delete
	    getType
	    getValueString
	    insert
	    getValueNumber
	    compareValueNumberTo
	    getValueBool
	    getSubNode
	    getSubNodes
	    getSubNodesAmount
	    getEnv
	    
	Env
	    Env
	    grow
	    lookup
	    define
	    update
	    _import
	    export
	    
	InterpSystemError
	    InterpSystemError
	InterpSystemExit
