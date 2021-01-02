/*
 * Inserts sensible test data about a library for computational algebraic
 * geometry (opencag). Please run on an clean database (as it results from
 * running 'setup.sql').
 */


INSERT INTO "user" (id, username, email_address, first_name, last_name,
    password_hash, password_salt, hashing_algorithm, is_admin) OVERRIDING SYSTEM VALUE VALUES
    (10, 'aldoconca', 'aldoconca@opencag.org', 'Aldo', 'Conca', 'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393', 'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25', 'SHA3-512', false),
    (11, 'uvonderohe', 'uvonderohe@opencag.org', 'Ulrich', 'von der Ohe', 'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393', 'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25', 'SHA3-512', false),
    (12, 'omidghayour', 'omidghayour@opencag.org', 'Omid', 'Ghayour', 'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393', 'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25', 'SHA3-512', false),
    (13, 'lauratorrente', 'lauratorrente@opencag.org', 'Laura', 'Torrente', 'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393', 'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25', 'SHA3-512', false),
    (14, 'wernermseiler', 'wernermseiler@t-online.at', 'Werner', 'Seiler', 'cb64f9739595a2eb5d58cb7a291aed0b0627f4efcbbf1a6b1c5e5864df3f6c941a0495fad7939cdd810bc74852a670ca14a9ae5033843c8d233d2a4f33b11393', 'aa35afbed60537ff39a5be70dc1d183fbf6614ea5ce7d36c2e5f154d2d3e1706d9429f8597fb12fd4d0601391aaa5684d15d8d0078645b4946acf5512766fc25', 'SHA3-512', false);

INSERT INTO topic (id, title, description) OVERRIDING SYSTEM VALUE VALUES
    (1, 'cagLib', 'About the core functionality of the opencag library. For issues concerning the command line or graphical interface, please turn to the respective topics.'),
    (2, 'CLI', 'About the command line interface of opencag.'),
    (3, 'cagIDE', 'About the graphical user inface of opencag.'),
    (4, 'Support', 'Support for external users.');

INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (104, 1, 'Timing SystemCommand', 'HINT', 'MINOR', '1.4.1', 12, '2016-02-03', 10, '2016-02-04', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (121, 104, E'<p>Hi,</p>
	<p>I was just timing some computations and noticed that the the time it takes to execute a command with <code>SystemCommand</code> is not accounted for with <code>CpuTime</code>.<br />While the manual more or less says that this is the expected behaviour, this is also quite inconvenient, since it is now not possible to accurately time any computations that involve calls to SystemCommand. :/</p>
	<p>Example:</p>
    <pre><code>t0:=CpuTime();
    SystemCommand("sleep 10");
    TimeFrom(t0);
    </code></pre>
	<p>Yes, I could time these computations using TimeOfDay(), but that only has a precision of seconds and may only bring up problems when timing around midnight...</p>
	<p>(Here is also a good spot to mention that I miss the simple but often useful <code>time</code> command that (AFAIR) opencag-0.8 offered ;) )</p>',
    12, '2016-02-27', 12, '2016-02-27'),
    (122, 104, E'<p>First comment: using <code>sleep</code> is perhaps not the best test. Below is what I get using bash on my Linux computer:<br /><pre>
$ time sleep 10

real    0m10.009s
user    0m0.000s
sys    0m0.006s
</pre><br />The <code>CpuTime</code> function measures "cpu-time" (which is the same as "user time", and may be considerably less than "wall-clock-time").</p>
	<p>Another problem is that the command executed by <code>SystemCommand</code> is a separate process, so it is not entirely clear that its time should be counted time consumed by CAG.<br />It is also not so easy to find a portable solution... Microsoft likes to do things its own way... :-/  And I am little inclined to waste my time working around Microsofts deliberate obstructionism.</p></div>',
    13, '2016-03-01', 13, '2016-03-02'),
    (123, 104, E'<p>I have now implemented <strong><code>ElapsedTime</code></strong> (based on the standard C++ <code>steady_clock</code>).</p>
<pre>
/**/ FloatStr(ElapsedTime()); SleepFor(5); FloatStr(ElapsedTime());
51.345
56.346
</pre>
	<p>Like <code>CpuTime()</code> the internal result is a <code>double</code> which is then converted exactly into a rational.  This means that one must use <code>FloatStr</code> to print out the result in a comprehensible way.  I wonder whether it may not simply be easier to make it return, say, milliseconds (as an integer)?</p></div>',
     13, '2016-03-04', 13, '2016-03-04'),
    (124, 104, E'<p>The new func <code>ElapsedTime</code> is perfectly fine with me!<br />I personally prefer the return value representing a rational as secs over an integer as millisecs, among others to also have it consistent with <code>CpuTime</code>.</p></div>',
     12, '2016-03-04', 12, '2016-03-04');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (105, 1, 'ker bug (quotientinghom for R/ideal())', 'BUG', 'SEVERE', '1.4.1', 10, '2016-03-21', 10, '2016-03-21', NULL, NULL, 1000);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (125, 105, E'<p>One of my students reports the following bug:<br /><pre>
/**/ R ::= QQ[x,y,z];
/**/ I := ideal(R,[]);
/**/ S := R/I;
/**/ phi := QuotientingHom(S);
/**/ ker(phi); --&gt; ERROR
</pre></p>',
    10, '2016-03-21', 10, '2016-03-21');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (106, 1, 'Kernel of quotient hom results in error', 'BUG', 'RELEVANT', '1.4.1', 10, '2016-03-23', 10, '2016-03-24', '2016-03-24', 105, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (126, 106, E'When I want to compute the kernel of quotientinghom of a quotient ring, I get an error!',
     11, '2016-03-23', 11, '2016-03-23'),
    (127, 106, E'Could you please provide a minimal example?',
     12, '2016-03-23', 12, '2016-03-23'),
    (128, 106, E'I have attached it to this post.',
     11, '2016-03-23', 11, '2016-03-23'),
    (129, 106, E'Seems to be a duplicate of <a href="/report/105">#105</a>.',
     12, '2016-03-24', 12, '2016-03-24');

INSERT INTO attachment (post, name, mimetype, content) OVERRIDING SYSTEM VALUE VALUES
    (128, 'quotientinghom-bug.txt', 'text/plain', E'R ::= QQ[x]; S := R/ideal(R, []); ker(QuotientingHom(S));');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (107, 1, 'F5 with 3 args?', 'HINT', 'RELEVANT', '1.4.3', 10, '2016-05-13', 10, '2016-05-13', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (156, 107, E'<p>In <code>TmpF5.H</code> two fns called <code>F5</code> are declared: one (inline) with 2 args, and one with 3 args (3rd arg has a default value).  The inline fn just calls the other one with 2 args (exploiting the presence of the default value for the 3rd arg).</p>
	<p>In <code>TmpF5.C</code> there is a defn of 2 arg fn called <code>F5</code>; the types match the types of the first 2 args in the 3 arg fn declared in <code>TmpF5.H</code>.</p>
	<p><code>test-F5.C</code> there is a call to the inlined 2 arg version of <code>F5</code>.  Somehow this call causes <code>GlobalManager</code> to report "Imminent disaster" during its destructor -- odd!</p>
	<p><strong>What I want to know is how this code ever compiled???</strong></p>
	<p>Anyone got any ideas?</p>',
     10, '2016-05-13', 10, '2016-05-13'),
    (157, 107, E'<p>If Arri comes my way I\'ll shorten his life by at least 2 hours -- the time I\'ve wasted tracking this bug down.  Grrr!</p>
	<p>Since the code compiles (without warning even) even with recent versions of <code>g++</code> I can only suppose that it is somehow valid... I\'m amazed, and also (negatively) shocked.</p>',
     10, '2016-05-13', 10, '2016-05-13'),
    (158, 107, E'<p>An easy workaround is just to comment out the 3rd arg in the declaration.<br />Since no one ever calls it with a 3rd arg (hopefully <strong>that</strong> would not compile!), I see no problems in doing this.</p>
	<p>I can see an imminent issue pushing for the removal of Arri\'s code!</p>',
     10, '2016-05-14', 10, '2016-05-14'),
    (159, 107, E'<p>So why did this problem not appear earlier?  And why did it appear now?</p>',
     12, '2016-05-14', 12, '2016-05-14'),
    (160, 107, E'<p>With 3rd formal arg commented out, it compiles and runs fine, but produces a different answer from before... :-/  At least no errors (or other nasty surprises) appear.</p>
	<p>Maybe I\'ll just disable the test.</p>',
     10, '2016-05-14', 10, '2016-05-14');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (108, 1, 'Function "ideal" evaluates the argument twice', 'BUG', 'SEVERE', '1.4.3', 12, '2016-05-21', 12, '2016-05-21', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (171, 108, E'<p><code>ideal(RING, func(...))</code> evaluates func once, <br /><code>ideal(func(...))</code> evaluates func twice.</p>',
     12, '2016-05-21', 12, '2016-05-21'),
    (172, 108, E'<p>Having just looked at the code, I\'m not entirely surprised.</p>
	<p>Also why are there calls to <code>evalArgAsListOfRingElem</code> and <code>evalArgAsListOf&lt;RingElem&gt;</code>?  :-/</p>
	<p>It seems to me that the arg has already been evaluated in the call to <code>evalArgAsT1orT2orT3</code>, and saved in the variable <code>x</code>.</p>',
     10, '2016-05-21', 10, '2016-05-21'),
    (173, 108, E'<p>Two test cases:<br /><pre>
define f(x) println "Inside f"; return x; enddefine;
I1 := ideal(f(x));   --&gt; BAD
I2 := ideal([f(x)]); --&gt; BAD
I3 := ideal(R, [f(x)]);--&gt;  OK
</pre></p>',
     10, '2016-05-21', 10, '2016-05-21'),
    (174, 108, E'<p>Here is some code we could add to one of the CAG tests:<br /><pre>
use R ::= QQ[x];
define f(x)
  TopLevel FLAG;
  if FLAG then error("Called f twice"); endif;
  FLAG := true;
  return x;
enddefine; -- f

FLAG := false;
I1 := ideal(f(x));

FLAG := false;
I2 := ideal([f(x)]);

FLAG := false;
I3 := ideal(R, [f(x)]);
</pre></p>


	<p>Probably the name of the top-level variable should be a bit longer (and more unusual).<br />The test prints nothing if all is well, otherwise a CAG error is reported.</p>',
     12, '2016-05-22', 12, '2016-05-23'),
    (175, 108, E'<p>Are there any other fns with a similar problem?  8-{</p>',
     10, '2016-05-24', 10, '2016-05-24'),
    (176, 108, E'<p>Oh dear!  The problem is more widespread.  Even <code>len</code> evaluates its arg twice 8-O</p>',
     10, '2016-05-24', 10, '2016-05-24');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (109, 1, 'Problem with template instantiation and order of include directives', 'BUG', 'MINOR', '1.4.3', 10, '2016-05-22', 10, '2016-05-22', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (181, 109, E'<p>Some versions of g++ (v4.3.2, v4.4.7, v4.6.3) and intel C++ compiler gave errors when compiling <code>ex-UtilsVector1.C</code> if, in the file <code>degree.H</code>, the <code>#include</code> directive for <code>UtilsVector.H</code> was placed before the other <code>#include</code> directives.  The error produced indicated that some prototypes for <code>cmp</code> were not visible (in ptic for two @MachineInt@s).</p>
	<p>We have no idea why, but since several compilers complain we wonder whether it isn\'t a strange limitation of C++.</p>
	<p>Logging the problem here.  The solution is just to move the <code>#include</code> directive in <code>degree.H</code> to after the other two.</p>',
     10, '2016-05-22', 10, '2016-05-22'),
    (182, 109, E'<p>JAA failed to find anything helpful on the internet.</p>
	<p>I\'m just hoping that the solution of moving the <code>#include</code> will be sufficient for the foreseeable future.</p>',
     10, '2016-05-23', 10, '2016-05-23'),
    (183, 109, E'<p>Clang 3.0 on my computer gives no error.</p>',
     11, '2016-05-23', 11, '2016-05-23'),
    (184, 109, E'<p>The problem persists with g++ 5.3.1.  It must be C++ thing, some weird restriction about calling "global" fns from inside template code.</p>',
     10, '2016-05-23', 10, '2016-05-23'),
    (185, 109, E'<p>It really is a C++ trap for the unwary... grrr!</p>
	<p>The following code fails to compile because the last line (<code>iter(vs);</code>) needs the second defn of <code>func</code>, but that is not visible at the point where the template fn was defined.  I\'m at a loss for words -- why does C++ have this "feature"???<br /><pre>
#include &lt;iostream&gt;
#include &lt;vector&gt;
#include &lt;string&gt;

using namespace std;

void func(int n)
{
  cout &lt;&lt; "int ";
}

template &lt;typename T&gt;
void iter(const std::vector&lt;T&gt;&#38; v)
{
  const int n = v.size();
  for (int i=0; i &lt; n; ++i)
    func(v[i]);
  cout &lt;&lt; endl;
}

void func(const std::string&#38; str)
{
  cout &lt;&lt; "str ";
}

int main()
{
  vector&lt;int&gt; vi; vi.push_back(1);
  vector&lt;string&gt; vs; vs.push_back("abc");
  iter(vi);
  iter(vs);
}
</pre></p>',
     10, '2016-05-24', 10, '2016-05-24');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (207, 2, 'Disable readline if input is redirected', 'BUG', 'SEVERE', '1.4.1', 10, '2016-03-26', 10, '2016-03-26', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (230, 207, E'<p>I was surprised to discover that <code>./cag < file.cag</code> echoed the contents of <code>file.cag</code>.</p>
	<p>It turns out that the input passes through <code>readline</code> which echoes every line.</p>
	<p>Currently one should execute <strong><code>./cag --no-readline < file.cag</code></strong></p>
	<p>This is inconvenient!</p>',
     10, '2016-03-26', 10, '2016-03-26');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (208, 2, 'Website: installation instructions for Microsoft', 'BUG', 'MINOR', '1.4.2', 10, '2016-04-03', 12, '2016-04-03', '2016-04-05', NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (231, 208, E'<p>The installation instructions for Microsoft refer to the directory <strong><code>C:\\cag-1.4.1\\emacs</code></strong> instead of <strong><code>C:\\cag-1.4.2\\emacs</code></strong></p>
	<p>Rectify</p>',
     10, '2016-04-03', 10, '2016-04-03'),
    (232, 208, E'<p>fixed</p>',
     12, '2016-04-05', 12, '2016-03-05');

INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (209, 2, 'Website: installation instructions for Microsoft', 'BUG', 'MINOR', '1.4.2', 12, '2016-04-09', 12, '2016-04-09', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (233, 209, E'<p>I tried a "fuzzing" test with CAG (feeding the executable as input).<br />CAG treats a NUL (ASCII code 0) as end-of-input; do we want this?</p>
	<p>Note that CAG treats EOT (ASCII 04) as end-of-input.</p>',
     12, '2016-04-09', 12, '2016-04-09'),
    (234, 209, E'<p>Maybe <code>parser::tryToRecover</code> is where one needs to look?</p>',
     10, '2016-04-10', 10, '2016-04-11'),
    (235, 209, E'<p>The relevant source code is mostly likely in <code>Lexer.C</code> around lines 136--137.<br />Inside <code>Lexer::getToken</code> there is a big <code>switch</code> statement which explicitly tests for <strong><code>\'\\0\'</code></strong>, and returns <code>Token::EndOfFile</code> in that case.</p>
	<p>I could just try commenting it out to see what happens... could be risky!</p>
	<p>If it is commented out then NUL would simply trigger an "Unknown symbol" exception (if nothing worse occurs).</p>',
     12, '2016-04-11', 12, '2016-04-11');


INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (401, 4, 'I CANNOT INSTALL IT', 'BUG', 'SEVERE', '1.4.2', 14, '2016-04-01', 10, '2016-04-02', NULL, NULL, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (431, 401, E'<p>IT DOESNT WIRK. WHEN I INSTALL IT SAYS "variable \'my_test_\' not in scope".</p>',
     14, '2016-04-01', 14, '2016-04-01'),
    (432, 401, E'<p>Could you <strong>please</strong> describe your problem more precisely? And use more appropriate language if you want to get help.</p>',
     11, '2016-04-02', 11, '2016-04-02'),
    (433, 401, E'<p>You chose "current version" as the target version of your report. I suppose you are referring to cag-1.4.2.</p>',
     12, '2016-04-02', 12, '2016-04-02'),
    (434, 401, E'<p>yes i have downgeloaded version 1.4.2</p>',
     14, '2016-04-02', 14, '2016-04-02'),
    (435, 401, E'<p>I don\'t understand your problem. From the error message, it looks like you successfully installed cag-1.4.2 and just tried to run a faulty script.</p>',
     12, '2016-04-02', 12, '2016-04-02'),
    (436, 401, E'Are you still interested in getting help?',
     11, '2016-04-08', 11, '2016-04-08');

INSERT INTO report (id, topic, title, type, severity, version, created_by, created_at, last_modified_by, last_modified_at, closed_at, duplicate_of, forced_relevance) OVERRIDING SYSTEM VALUE VALUES
    (402, 4, 'I CANNOT INSTALL IT', 'BUG', 'SEVERE', '1.4.2', 14, '2016-04-01', 10, '2016-04-02', '2016-04-02', 401, NULL);

INSERT INTO post (id, report, content, created_by, created_at, last_modified_by, last_modified_at) OVERRIDING SYSTEM VALUE VALUES
    (441, 402, E'<p>IT DOESNT WIRK. WHEN I INSTALL IT SAYS "variable \'my_test_\' not in scope".</p>',
     14, '2016-04-01', 14, '2016-04-01');
