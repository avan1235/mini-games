import UIKit
import SwiftUI
import shared_client

struct ComposeView: UIViewControllerRepresentable {
    
    let component: MiniGamesAppComponent
    
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.MainViewController(component: component)
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    
    let component: MiniGamesAppComponent
    
    @Environment(\.colorScheme) var colorScheme
    
    var body: some View {
        ComposeView(component: component)
            .ignoresSafeArea(.all)
    }
}

