import java.io.*;

class Calc {
    int token; int value; int ch;
    private PushbackInputStream input;
    final int NUMBER=256;

    Calc(PushbackInputStream is) {
        input = is;
    }

    int getToken( )  { /* tokens are characters */
        while(true) {
            try  {
                ch = input.read();
                if (ch == ' ' || ch == '\t' || ch == '\r') ;
                else
                if (Character.isDigit(ch)) {
                    value = number( );
                    input.unread(ch);
                    return NUMBER;
                }
                else return ch;
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }

    private int number( )  {
        /* number -> digit { digit } */
        int result = ch - '0';
        try  {
            ch = input.read();
            while (Character.isDigit(ch)) {
                result = 10 * result + ch -'0';
                ch = input.read();
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return result;
    }

    void error( ) {
        System.out.printf("parse error : %d\n", ch);
        //System.exit(1);
    }

    void match(int c) {
        if (token == c)
            token = getToken();
        else error();
    }

    void command( ) {
        /* command -> expr '\n' */
        Object result = expr();  // boolean / int
        if (token == '\n') /* end the parse and print the result */
            System.out.println("The result is : " + result);
        else error();
    }

    Object expr() {
        /* <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr> | true | false */
        Object result;
        if (token == '!'){
            // !<expr>
            match('!');
            result = !(boolean) expr();
        } else if (token == 't'){
            // true
            match('t');
            result = true;
        } else if (token == 'f'){
            // false
            match('f');
            result = false;
        } else {
            /* <bexp> {& <bexp> | '|'<bexp>} */
            result = bexp();
            while (token == '&' || token == '|') {
                if (token == '&'){
                    match('&');
                    result = (boolean)bexp() & (boolean)result;
                }
                else {
                    match('|');
                    result = (boolean)bexp() | (boolean)result;
                }
            }
        }
        return result;
    }

    Object bexp( ) {
        /* <bexp> -> <aexp> [<relop> <aexp>] */
        Object result = null;
        int aexp1 = aexp();
        if (token == '<' || token == '>' || token == '=' || token == '!') { // <relop>
            /* Check each string using relop(): "<", "<=", ">", ">=", "==", "!=" */
            String op = relop();    // relop()을 통해 op에 비교연산자 저장
            int aexp2 = aexp();

            switch (op) {
                case "<":
                    return aexp1 < aexp2;
                case "<=":
                    return aexp1 <= aexp2;
                case ">":
                    return aexp1 > aexp2;
                case ">=":
                    return aexp1 >= aexp2;
                case "==":
                    return aexp1 == aexp2;
                case "!=":
                    return aexp1 != aexp2;
                default:
                    error();
                    return null;
            }
        } else {
            return  aexp1; //  비교 연산자 없을 시 산술 결과만 반환
        }
    }

    String relop() {
        /* <relop> -> ( < | <= | > | >= | == | != ) */
        String result = "";

        if (token == '<') {
            match('<');
            if (token == '=') {
                match('=');
                result = "<=";
            } else {
                result = "<";
            }
        } else if (token == '>') {
            match('>');
            if (token == '=') {
                match('=');
                result = ">=";
            } else {
                result = ">";
            }
        } else if (token == '=') {
            match('=');
            if (token == '=') {
                match('=');
                result = "==";
            } else {
                error();
            }
        } else if (token == '!') {
            match('!');
            if (token == '=') {
                match('=');
                result = "!=";
            } else {
                error();
            }
        } else {
            error();
        }
        return result;
    }

    int aexp() {
        /* <aexp> -> <term> { '+' <term> | '-' <term> } */
        int result = term();
        while (token == '+' || token == '-') {  // loop 에 '-' 추가
            if (token == '+') {
                match('+');
                result += term();
            } else {
                match('-');
                result -= term();
            }

        }
        return result;
    }

    int term( ) {
        /* <term> -> <factor> { '*' <factor> | '/' <factor> } */
        int result = factor();
        while (token == '*' || token == '/') {
            if (token == '*') {
                match('*');
                result *= factor();
            } else {
                match('/');
                int divisor = factor(); // factor()로 변수에 저장
                if (divisor != 0) { // divide to 0 예외 처리
                    result /= divisor;
                } else {
                    System.err.println("Error: Division by zero");
                }
            }
        }
        return result;
    }

    int factor() {
        /* factor -> '-' factor | '(' expr ')' | number */
        int result = 0;

        // 음수 처리
        if (token == '-') {
            match('-');
            result = -factor(); // factor() 재귀 호출 -> 음수 처리
        }
        else if (token == '(') {
            match('(');
            result = aexp();   // 산술 연산
            match(')');
        }
        // 숫자 처리
        else if (token == NUMBER) {
            result = value;
            match(NUMBER);
        }
        return result;
    }

    void parse( ) {
        token = getToken(); // get the first token
        command();          // call the parsing command
    }

    public static void main(String args[]) {
        Calc calc = new Calc(new PushbackInputStream(System.in));
        while(true) {
            System.out.print(">> ");
            calc.parse();
        }
    }
}