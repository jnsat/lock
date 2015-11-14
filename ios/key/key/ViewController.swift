//
//  ViewController.swift
//  key
//
//  Created by jns on 9/19/15.
//  Copyright © 2015 fun. All rights reserved.
//  YAAAAAAAAAAAAAAAAAAAAHHHHH

import UIKit

let port = 2390;
let ip = "192.168.0.139";
class ViewController: UIViewController {
    @IBOutlet var lock: UIButton!
    @IBAction func unlock(sender: UIButton) {
    }
    @IBAction func lock(sender: AnyObject) {
    }
    override func viewDidLoad() {
        super.viewDidLoad()
        @IBOutlet var unlock: UIButton!
        @IBOutlet var lcok: UIButton!
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

/* https://github.com/swiftsocket/SwiftSocket/blob/master/SwiftSocket/main.swift */
func send(){
    let client:UDPClient = UDPClient(addr: ip, port: port)
    print("send hello world")
    client.send(str: "hello world")
    client.close()
}
