import UIKit
import SwiftUI
import shared_client

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
            .edgesIgnoringSafeArea(.bottom)
            .overlay(alignment: .top, content: {
                Color(red: 0.208, green: 0.208, blue: 0.208)
                    .background(.regularMaterial)
                    .edgesIgnoringSafeArea(.top)
                    .frame(height: 0)
            })
    }
}
