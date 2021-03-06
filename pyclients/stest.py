#!/usr/bin/python

""" Serially sends 500 requests to the tinybookstore server separated 
by time intervals specified in argv"""

import xmlrpclib, sys, time
from random import randint
import threading

name = "http://" + sys.argv[1] + ":" + sys.argv[2]

item_nums = [53477, 53573, 12365, 12498]
topics = ["distributed systems", "college life"]

def stest():
    server  = xmlrpclib.Server(name)
    total_time = 0
    start_time = 0
    for i in range(500):
        r = randint(0,2)
        if r == 0:
            r = randint(0,3)
            start_time = time.time()
            server.bookstore.lookup(item_nums[r])
        elif r == 1:
            r = randint(0,1)
            start_time = time.time()
            server.bookstore.search(topics[r])
        else:
            r = randint(0,3)
            start_time = time.time()
            server.bookstore.buy(item_nums[r])
        end_time = time.time()
        total_time += (end_time - start_time)
    print "Average time per request {}".format(total_time/500)

def buy500():
    server  = xmlrpclib.Server(name)
    total_time = 0
    for i in range(500):
        r = randint(0,3)
        start_time = time.time()    
        server.bookstore.buy(item_nums[r])
        end_time = time.time()
        total_time += (end_time - start_time)
    print "Average time per buy {}".format(total_time/500)

results = [None]*50
threads = [None]*50

def send10(result, index):
    server = xmlrpclib.Server(name)
    total_time = 0
    for i in range(10):
        r = randint(0,3)
        start_time = time.time()    
        server.bookstore.buy(item_nums[r])
        end_time = time.time()
        total_time += (end_time - start_time)
    result[index] = total_time
    
def main():
    for i in range(len(threads)):
        threads[i] = threading.Thread(target=send10, args=(results,i))
        threads[i].start()
    
    for i in range(len(threads)):
        threads[i].join()

    print "Time per request: {}".format(sum(results)/500)

if __name__ == "__main__":
    main()
