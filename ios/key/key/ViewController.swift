//
//  ViewController.swift
//  key
//
//  Created by jns on 9/19/15.
//  Copyright © 2015 fun. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        let pad = 16;
        let h = 32;
        let w = 64;
        let rect = CGRect(x: pad, y: pad, width: w, height: h);
        let b_lock = UIButton(frame: rect);
        let b_unlock = UIButton(frame: rect);
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

