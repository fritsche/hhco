# HH-CO

This program mainly implements the HH-CO algorithm, described in our paper:

Gian Fritsche and Aurora Pozo. 2019. Cooperative based Hyper-heuristic for Many-objective Optimization. In Genetic and Evolutionary Computation Conference (GECCO ’19), July 13–17, 2019, Prague, Czech Republic.ACM, New York, NY, USA, 9 pages. https://doi.org/10.1145/3321707.3321740

--- 

The code was written in Java and based on the jMetal framework. 
Some algorithms were taken from jMetal (https://github.com/jMetal/jMetal);
others from Dr. Yuan Yuan repository (https://github.com/yyxhdy/ManyEAs) or by e-mail.

Note that, this code can be used only for non-commercial purposes. 
We would appreciate your acknowledgment if you use the code.

For any problem concerning the code, please fill in an issue: https://github.com/fritsche/hhco/issues.

---

```
usage: java -cp <jar> br.ufpr.inf.cbio.hhco.runner.HHCORunner [-h] [-id
       <id>] [-m <objectives>] [-p <problem>] [-P <path>] [-s <seed>]

Execute a single independent run of the HHCO algorithm on a given
<problem>.
 -h,--help                      print this message and exits.
 -id <id>                       set the independent run id, default 0.
 -m,--objectives <objectives>   set the number of objectives (default
                                value is 5).
 -p,--problem <problem>         set the problem instance: MaF[1-15],
                                default MaF01
 -P,--output-path <path>        directory path for output (if no path is
                                given experiment/ will be used.)
 -s,--seed <seed>               set the seed for JMetalRandom, default
                                System.currentTimeMillis()

Please report issues at https://github.com/fritsche/hhco/issues
```
