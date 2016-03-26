#!/usr/local/bin/python

""" A TinyBookstore client. Only supports lookup(item_num), search(topic), buy(item_num).
(c) 2016. modsoussi. """

import xmlrpclib, sys

name = "http://" + sys.argv[1] + ":" + sys.argv[2]

server = xmlrpclib.Server(name)

def main():
    while(True):
        inst = raw_input("> TinyBookstore.com: ").split(' ', 1)
        if inst[0] == "lookup":
            if len(inst) > 1:
                ans = server.bookstore.lookup(int(inst[1]))
                if len(ans) > 0:
                    print ans
                else:
                    print "Book not in database."
        if inst[0] == "search":
            query = ""
            if len(inst) > 1:
                query = inst[1]
            ans = server.bookstore.search(query)
            if len(ans) > 0:
                print "Search Results:\n" + ans
            else:
                print "No results."
        if inst[0] == "buy":
            ans = server.bookstore.buy(int(inst[1]))
            print ans

if __name__== "__main__":
    main()
